package info.jashan.backend.model;

import java.util.Map;

public record Snapshot(
        long ts,
        Map<String, PerSymbol> perSymbol,
        Totals totals,
        Limits limits,
        Health health,
        int tradesPerSec,
        String status,
        java.util.List<Alert> alerts
) {
    public record PerSymbol(int qty, double avgPrice, double mid, double unrealizedPnL, double exposure) {}
    public record Totals(double netExposure, double grossExposure, double realizedPnL, double unrealizedPnL, double totalPnL) {}
}