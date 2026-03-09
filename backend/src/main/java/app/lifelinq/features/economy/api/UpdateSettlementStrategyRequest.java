package app.lifelinq.features.economy.api;

import app.lifelinq.features.economy.domain.SettlementStrategyType;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public final class UpdateSettlementStrategyRequest {
    private SettlementStrategyType strategyType;
    private Map<UUID, BigDecimal> percentageShares;

    public SettlementStrategyType getStrategyType() {
        return strategyType;
    }

    public void setStrategyType(SettlementStrategyType strategyType) {
        this.strategyType = strategyType;
    }

    public Map<UUID, BigDecimal> getPercentageShares() {
        return percentageShares;
    }

    public void setPercentageShares(Map<UUID, BigDecimal> percentageShares) {
        this.percentageShares = percentageShares;
    }
}
