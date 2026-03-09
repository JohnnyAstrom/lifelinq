package app.lifelinq.features.economy.application;

import app.lifelinq.features.economy.domain.SettlementPeriod;
import app.lifelinq.features.economy.domain.SettlementPeriodRepository;
import app.lifelinq.features.economy.domain.SettlementStrategySnapshot;
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
    public void execute(UUID periodId, SettlementStrategySnapshot strategySnapshot) {
        if (periodId == null) {
            throw new IllegalArgumentException("periodId must not be null");
        }
        if (strategySnapshot == null) {
            throw new IllegalArgumentException("strategySnapshot must not be null");
        }
        SettlementPeriod period = settlementPeriodRepository.findById(periodId)
                .orElseThrow(() -> new IllegalArgumentException("period not found"));
        SettlementPeriod updated = period.withStrategySnapshot(strategySnapshot);
        settlementPeriodRepository.save(updated);
    }
}
