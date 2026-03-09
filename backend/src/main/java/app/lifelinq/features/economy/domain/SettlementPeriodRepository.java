package app.lifelinq.features.economy.domain;

import java.util.Optional;
import java.util.UUID;

public interface SettlementPeriodRepository {
    void save(SettlementPeriod period);

    Optional<SettlementPeriod> findById(UUID periodId);

    Optional<SettlementPeriod> findOpenByGroupId(UUID groupId);

    boolean existsOpenByGroupId(UUID groupId);
}
