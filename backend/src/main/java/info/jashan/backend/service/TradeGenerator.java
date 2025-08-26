package info.jashan.backend.service;

import info.jashan.backend.model.Trade;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.List;
import java.util.Random;

public class TradeGenerator {
    private final Sinks.Many<Trade> sink = Sinks.many().multicast().onBackpressureBuffer();
    private final Random rnd = new Random();
    private volatile Mode mode = Mode.NORMAL;

    public enum Mode { NORMAL, VOLATILITY, BURST, FEED_DOWN }

    private final List<String> symbols = List.of("AAPL","MSFT","AMZN","TSLA","NVDA");

    public TradeGenerator() {
        // emit trades on an interval; rate depends on mode
        Flux.interval(Duration.ofMillis(20)) // base 50 tps
                .map(t -> generate())
                .subscribe(trade -> {
                    if (mode != Mode.FEED_DOWN) sink.tryEmitNext(trade);
                });
    }

    private Trade generate() {
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
        double vol = switch (mode) { case VOLATILITY -> 0.02; case BURST -> 0.01; default -> 0.008; };
        double price = mid * (1 + (rnd.nextGaussian() * vol));
        Trade.Side side = rnd.nextBoolean() ? Trade.Side.BUY : Trade.Side.SELL;
        return new Trade(sym, side, qty, price, System.currentTimeMillis());
    }

    public Flux<Trade> flux() { return sink.asFlux(); }

    public void setMode(Mode m) { this.mode = m; }
    public Mode getMode() { return mode; }
}