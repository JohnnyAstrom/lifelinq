package app.lifelinq.features.economy.application;

import app.lifelinq.features.economy.domain.SettlementPeriod;
import app.lifelinq.features.economy.domain.SettlementPeriodRepository;
import app.lifelinq.features.economy.domain.SettlementStrategySnapshot;
import app.lifelinq.features.economy.domain.SettlementStrategyType;
import app.lifelinq.features.group.contract.AccessDeniedException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public final class UpdateSettlementStrategyUseCase {
    private final SettlementPeriodRepository settlementPeriodRepository;

    public UpdateSettlementStrategyUseCase(SettlementPeriodRepository settlementPeriodRepository) {
        if (settlementPeriodRepository == null) {
            throw new IllegalArgumentException("settlementPeriodRepository must not be null");
        }
        this.settlementPeriodRepository = settlementPeriodRepository;
    }

    @Transactional
    public void execute(
            UUID activeGroupId,
            UUID periodId,
            SettlementStrategyType strategyType,
            Map<UUID, BigDecimal> percentageShares
    ) {
        if (activeGroupId == null) {
            throw new IllegalArgumentException("activeGroupId must not be null");
        }
        if (periodId == null) {
            throw new IllegalArgumentException("periodId must not be null");
        }
        if (strategyType == null) {
            throw new IllegalArgumentException("strategyType must not be null");
        }
        SettlementPeriod period = settlementPeriodRepository.findById(periodId)
                .orElseThrow(() -> new PeriodNotFoundException(periodId));
        if (!activeGroupId.equals(period.getGroupId())) {
            throw new AccessDeniedException("period does not belong to active group");
        }
        SettlementStrategySnapshot strategySnapshot = strategyType == SettlementStrategyType.PERCENTAGE_COST
                ? SettlementStrategySnapshot.percentageCost(percentageShares)
                : SettlementStrategySnapshot.of(strategyType);
        SettlementPeriod updated = period.withStrategySnapshot(strategySnapshot);
        settlementPeriodRepository.save(updated);
    }
}
