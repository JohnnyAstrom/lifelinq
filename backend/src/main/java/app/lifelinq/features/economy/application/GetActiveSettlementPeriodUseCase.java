package app.lifelinq.features.economy.application;

import app.lifelinq.features.economy.domain.SettlementPeriod;
import app.lifelinq.features.economy.domain.SettlementPeriodRepository;
import java.util.Optional;
import java.util.UUID;

public final class GetActiveSettlementPeriodUseCase {
    private final SettlementPeriodRepository settlementPeriodRepository;
    private final InitializeGroupEconomyUseCase initializeGroupEconomyUseCase;

    public GetActiveSettlementPeriodUseCase(
            SettlementPeriodRepository settlementPeriodRepository,
            InitializeGroupEconomyUseCase initializeGroupEconomyUseCase
    ) {
        if (settlementPeriodRepository == null) {
            throw new IllegalArgumentException("settlementPeriodRepository must not be null");
        }
        if (initializeGroupEconomyUseCase == null) {
            throw new IllegalArgumentException("initializeGroupEconomyUseCase must not be null");
        }
        this.settlementPeriodRepository = settlementPeriodRepository;
        this.initializeGroupEconomyUseCase = initializeGroupEconomyUseCase;
    }

    public Optional<SettlementPeriod> execute(UUID groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        Optional<SettlementPeriod> activePeriod = settlementPeriodRepository.findOpenByGroupId(groupId);
        if (activePeriod.isPresent()) {
            return activePeriod;
        }
        initializeGroupEconomyUseCase.initializeGroupEconomy(groupId);
        return settlementPeriodRepository.findOpenByGroupId(groupId);
    }
}
