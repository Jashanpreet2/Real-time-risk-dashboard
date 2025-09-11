package info.jashan.backend.service;

import info.jashan.backend.model.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RiskEngine {
    private final Sinks.Many<Snapshot> snapshots = Sinks.many().replay().latest();
    private final TradeGenerator tradeGenerator;
    private volatile Limits limits = new Limits(5_000_000, 1_500_000, 250_000);
    private volatile Health health = new Health(true, true, true);
    private volatile double peakTotalPnL = 0.0;
    private volatile int tradesInWindow = 0;

    public RiskEngine(Flux<Trade> tradeFlux, TradeGenerator tradeGenerator) {
        this.tradeGenerator = tradeGenerator;
        tradeFlux.subscribe(this::onTrade);
        Flux.interval(Duration.ofMillis(250)).subscribe(t -> publishSnapshot());
        Flux.interval(Duration.ofSeconds(1)).subscribe(t -> tradesInWindow = 0); // reset TPS counter window
    }

    private void onTrade(Trade tr) {
        tradesInWindow++;
    }

    private void publishSnapshot() {
        long now = System.currentTimeMillis();
        Map<String, Snapshot.PerSymbol> per = new HashMap<>();
        double net = 0, gross = 0, unreal = 0;
        List<Alert> alerts = new ArrayList<>();

        for (var e : tradeGenerator.getPositions().entrySet()) {
            String sym = e.getKey();
            Position pos = e.getValue();
            double mid = tradeGenerator.getMids().getOrDefault(sym, pos.avgPrice());
            double exposure = pos.qty() * mid;
            double upnl = pos.qty() * (mid - pos.avgPrice());
            per.put(sym, new Snapshot.PerSymbol(pos.qty(), pos.avgPrice(), mid, upnl, Math.abs(exposure)));
            net += exposure;
            gross += Math.abs(exposure);
            unreal += upnl;
            if (Math.abs(exposure) > limits.singleNameLimit()) {
                alerts.add(new Alert("SINGLE_NAME_LIMIT", sym + " exposure " + fmt(exposure) + " > limit", now));
            }
        }

        double total = tradeGenerator.getRealizedPnL() + unreal;
        if (total > peakTotalPnL) peakTotalPnL = total;
        if (gross > limits.grossExposureLimit()) {
            alerts.add(new Alert("GROSS_EXPOSURE_LIMIT", "Gross " + fmt(gross) + " > limit", now));
        }
        if ((peakTotalPnL - total) > limits.drawdownLimit()) {
            alerts.add(new Alert("DRAWDOWN_LIMIT", "Drawdown " + fmt(peakTotalPnL - total) + " > limit", now));
        }

        if (!health.marketDataFeed()) {
            alerts.add(new Alert("FEED_DOWN", "Market data feed down", now));
        }

        Snapshot.Totals totals = new Snapshot.Totals(net, gross, tradeGenerator.getRealizedPnL(), unreal, total);
        Snapshot snap = new Snapshot(now, per, totals, limits, health, tradesInWindow, alerts.isEmpty()? "OK":"ALERT", alerts);
        snapshots.tryEmitNext(snap);
    }

    public Flux<Snapshot> snapshots() { return snapshots.asFlux(); }

    public void setMode(TradeGenerator.Mode mode, TradeGenerator generator) {
        if (mode == TradeGenerator.Mode.FEED_DOWN) {
            this.health = new Health(false, true, true);
        } else {
            this.health = new Health(true, true, true);
        }
        generator.setMode(mode);
    }

    public void recover(TradeGenerator generator) {
        this.health = new Health(true, true, true);
        generator.setMode(TradeGenerator.Mode.NORMAL);
    }

    public Limits getLimits() { return limits; }
    public void setLimits(Limits l) { this.limits = l; }

    private static String fmt(double v){
        return String.format("%,.0f", v);
    }
}