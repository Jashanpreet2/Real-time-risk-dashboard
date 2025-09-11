package info.jashan.backend.service;

import info.jashan.backend.model.Position;
import info.jashan.backend.model.Trade;
import lombok.Getter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class TradeGenerator {
    private int displayCount = 0;
    @Getter
    private final Map<String, Position> positions = new ConcurrentHashMap<>();
    private final Sinks.Many<Trade> sink = Sinks.many().multicast().onBackpressureBuffer();
    private final Random rnd = new Random();

    private volatile Mode mode = Mode.NORMAL;
    @Getter
    private final Map<String, Double> mids = new ConcurrentHashMap<>();
    @Getter
    private volatile double realizedPnL = 0.0;



    public enum Mode { NORMAL, VOLATILITY, BURST, FEED_DOWN }

    private final List<String> symbols = List.of("AAPL","MSFT","AMZN","TSLA","NVDA");


    public TradeGenerator() {
        // emit trades on an interval; rate depends on mode
        Flux.interval(Duration.ofMillis(20)) // base 50 tps
                .map(t -> generate())
                .subscribe(trade -> {
                    if (mode != Mode.FEED_DOWN) sink.tryEmitNext(trade);
                });
        for (String sym: symbols){
            double price = switch (sym) {
                case "AAPL" -> 190;
                case "MSFT" -> 420;
                case "AMZN" -> 170;
                case "TSLA" -> 250;
                default -> 1000; // NVDA
            };
            positions.put(sym, new Position(20000, price));
        }
    }

    private Trade generate() {
        if (displayCount < 5) {
        System.out.println("Outputting the quanities of len " + positions.size());
        for (String sym: positions.keySet()) {
            System.out.println("Symbol: " + sym + " Quantity: " + positions.get(sym).qty());
        }
        displayCount += 1;
    }
        String sym = symbols.get(rnd.nextInt(symbols.size()));
        int baseQty = switch (mode) { case BURST -> 200; default -> 50; };
        int qty = (rnd.nextInt(baseQty) + 1) * (rnd.nextBoolean()? 1: 1); // 1..baseQty
        double mid = switch (sym) {
            case "AAPL" -> 190;
            case "MSFT" -> 420;
            case "AMZN" -> 170;
            case "TSLA" -> 250;
            default -> 1000; // NVDA
        };
        double vol = switch (mode) { case VOLATILITY -> 0.02; case BURST -> 0.01; default -> 0.00008; };
        double price = mid * (1 + (rnd.nextGaussian() * vol));
        Position position = positions.getOrDefault(sym, null);
        Trade.Side side = null;

        if  (position == null || position.qty() < qty) {
            side = Trade.Side.BUY;
        } else {
            side = rnd.nextBoolean() ? Trade.Side.BUY : Trade.Side.SELL;
        }

        Trade tr = new Trade(sym, side, qty, price, System.currentTimeMillis());

        mids.merge(tr.symbol(), tr.price(), (a,b) -> b); // latest price as mid for simplicity
        Position p = positions.getOrDefault(tr.symbol(), new Position(0, 0));
        double avg = p.avgPrice();

        if (tr.side() == Trade.Side.BUY) {
            double newAvg = (p.qty() * avg + tr.qty() * tr.price()) / (p.qty() + tr.qty());
            positions.put(tr.symbol(), new Position(p.qty() + tr.qty(), newAvg));
        } else {
            int sellQty = tr.qty();
            int newQty = p.qty() - sellQty;
            // realized PnL on closed quantity (simple model)
            int closed = Math.min(p.qty(), sellQty);
            realizedPnL += closed * (tr.price() - avg);
            if (newQty <= 0) {
                positions.put(tr.symbol(), new Position(newQty, 0));
            } else {
                positions.put(tr.symbol(), new Position(newQty, avg));
            }
        }
        return tr;
    }

    public Flux<Trade> flux() { return sink.asFlux(); }

    public void setMode(Mode m) { this.mode = m; }
    public Mode getMode() { return mode; }
}