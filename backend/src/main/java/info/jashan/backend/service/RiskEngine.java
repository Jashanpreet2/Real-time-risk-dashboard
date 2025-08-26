package info.jashan.backend.service;

import info.jashan.backend.model.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RiskEngine {
    private final Map<String, Position> positions = new ConcurrentHashMap<>();
    private final Map<String, Double> mids = new ConcurrentHashMap<>();
    private final Sinks.Many<Snapshot> snapshots = Sinks.many().replay().latest();

    private volatile double realizedPnL = 0.0;
    private volatile Limits limits = new Limits(5_000_000, 1_500_000, 250_000);
    private volatile Health health = new Health(true, true, true);
    private volatile double peakTotalPnL = 0.0;
    private volatile int tradesInWindow = 0;

    public RiskEngine(Flux<Trade> tradeFlux) {
        tradeFlux.subscribe(this::onTrade);
        Flux.interval(Duration.ofMillis(250)).subscribe(t -> publishSnapshot());
        Flux.interval(Duration.ofSeconds(1)).subscribe(t -> tradesInWindow = 0); // reset TPS counter window
    }

    private void onTrade(Trade tr) {
        tradesInWindow++;
        mids.merge(tr.symbol(), tr.price(), (a,b) -> b); // latest price as mid for simplicity
        Position p = positions.getOrDefault(tr.symbol(), new Position(0, 0));
        int qty = p.qty();
        double avg = p.avgPrice();

        if (tr.side() == Trade.Side.BUY) {
            // new avg price
            double newAvg = (qty * avg + tr.qty() * tr.price()) / (qty + tr.qty());
            positions.put(tr.symbol(), new Position(qty + tr.qty(), newAvg));
        } else {
            int sellQty = tr.qty();
            int newQty = qty - sellQty;
            // realized PnL on closed quantity (simple model)
            int closed = Math.min(qty, sellQty);
            realizedPnL += closed * (tr.price() - avg);
            if (newQty <= 0) {
                positions.put(tr.symbol(), new Position(newQty, 0));
            } else {
                positions.put(tr.symbol(), new Position(newQty, avg));
            }
        }
    }

    private void publishSnapshot() {
        long now = System.currentTimeMillis();
        Map<String, Snapshot.PerSymbol> per = new HashMap<>();
        double net = 0, gross = 0, unreal = 0;
        List<Alert> alerts = new ArrayList<>();

        for (var e : positions.entrySet()) {
            String sym = e.getKey();
            Position pos = e.getValue();
            double mid = mids.getOrDefault(sym, pos.avgPrice());
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

        double total = realizedPnL + unreal;
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

        Snapshot.Totals totals = new Snapshot.Totals(net, gross, realizedPnL, unreal, total);
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