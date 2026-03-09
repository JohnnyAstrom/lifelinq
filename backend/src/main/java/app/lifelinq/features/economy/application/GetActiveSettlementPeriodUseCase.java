package app.lifelinq.features.economy.application;

import app.lifelinq.features.economy.domain.SettlementPeriod;
import app.lifelinq.features.economy.domain.SettlementPeriodRepository;
import java.util.Optional;
import java.util.UUID;

public final class GetActiveSettlementPeriodUseCase {
    private final SettlementPeriodRepository settlementPeriodRepository;

    public GetActiveSettlementPeriodUseCase(SettlementPeriodRepository settlementPeriodRepository) {
        if (settlementPeriodRepository == null) {
            throw new IllegalArgumentException("settlementPeriodRepository must not be null");
        }
        this.settlementPeriodRepository = settlementPeriodRepository;
    }

    public Optional<SettlementPeriod> execute(UUID groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        return settlementPeriodRepository.findOpenByGroupId(groupId);
    }
}
