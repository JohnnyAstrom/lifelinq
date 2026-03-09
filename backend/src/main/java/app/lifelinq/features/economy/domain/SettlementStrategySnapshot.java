package app.lifelinq.features.economy.domain;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class SettlementStrategySnapshot {
    private final SettlementStrategyType strategyType;
    private final Map<UUID, BigDecimal> percentageShares;

    private SettlementStrategySnapshot(
            SettlementStrategyType strategyType,
            Map<UUID, BigDecimal> percentageShares
    ) {
        if (strategyType == null) {
            throw new IllegalArgumentException("strategyType must not be null");
        }
        Map<UUID, BigDecimal> normalized = normalizePercentageShares(strategyType, percentageShares);
        this.strategyType = strategyType;
        this.percentageShares = Collections.unmodifiableMap(normalized);
    }

    public static SettlementStrategySnapshot equalCost() {
        return new SettlementStrategySnapshot(SettlementStrategyType.EQUAL_COST, Map.of());
    }

    public static SettlementStrategySnapshot of(SettlementStrategyType strategyType) {
        return new SettlementStrategySnapshot(strategyType, Map.of());
    }

    public static SettlementStrategySnapshot percentageCost(Map<UUID, BigDecimal> percentageShares) {
        return new SettlementStrategySnapshot(SettlementStrategyType.PERCENTAGE_COST, percentageShares);
    }

    public SettlementStrategyType getStrategyType() {
        return strategyType;
    }

    public Map<UUID, BigDecimal> getPercentageShares() {
        return percentageShares;
    }

    private static Map<UUID, BigDecimal> normalizePercentageShares(
            SettlementStrategyType strategyType,
            Map<UUID, BigDecimal> percentageShares
    ) {
        if (strategyType != SettlementStrategyType.PERCENTAGE_COST) {
            return Map.of();
        }
        if (percentageShares == null || percentageShares.isEmpty()) {
            throw new IllegalArgumentException("percentageShares must not be empty for PERCENTAGE_COST");
        }
        Map<UUID, BigDecimal> normalized = new LinkedHashMap<>();
        BigDecimal sum = BigDecimal.ZERO;
        for (Map.Entry<UUID, BigDecimal> entry : percentageShares.entrySet()) {
            UUID userId = entry.getKey();
            BigDecimal percentage = entry.getValue();
            if (userId == null) {
                throw new IllegalArgumentException("percentageShares userId must not be null");
            }
            if (percentage == null || percentage.signum() < 0) {
                throw new IllegalArgumentException("percentageShares values must be >= 0");
            }
            normalized.put(userId, percentage);
            sum = sum.add(percentage);
        }
        if (sum.compareTo(new BigDecimal("100")) != 0) {
            throw new IllegalArgumentException("percentageShares must sum to 100");
        }
        return normalized;
    }
}
