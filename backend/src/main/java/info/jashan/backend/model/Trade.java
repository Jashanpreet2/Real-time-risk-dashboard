package info.jashan.backend.model;

public record Trade(String symbol, Side side, int qty, double price, long ts) {
    public enum Side { BUY, SELL }
}