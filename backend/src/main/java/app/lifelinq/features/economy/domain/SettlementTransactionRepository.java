package app.lifelinq.features.economy.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SettlementTransactionRepository {
    void save(SettlementTransaction transaction);

    Optional<SettlementTransaction> findById(UUID transactionId);

    List<SettlementTransaction> findByPeriodId(UUID periodId);

    List<SettlementTransaction> findActiveByPeriodId(UUID periodId);
}
