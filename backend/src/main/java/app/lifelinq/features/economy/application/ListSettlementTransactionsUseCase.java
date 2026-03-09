package app.lifelinq.features.economy.application;

import app.lifelinq.features.economy.domain.SettlementPeriodRepository;
import app.lifelinq.features.economy.domain.SettlementTransaction;
import app.lifelinq.features.economy.domain.SettlementTransactionRepository;
import java.util.List;
import java.util.UUID;

public final class ListSettlementTransactionsUseCase {
    private final SettlementTransactionRepository settlementTransactionRepository;
    private final SettlementPeriodRepository settlementPeriodRepository;

    public ListSettlementTransactionsUseCase(
            SettlementTransactionRepository settlementTransactionRepository,
            SettlementPeriodRepository settlementPeriodRepository
    ) {
        if (settlementTransactionRepository == null) {
            throw new IllegalArgumentException("settlementTransactionRepository must not be null");
        }
        if (settlementPeriodRepository == null) {
            throw new IllegalArgumentException("settlementPeriodRepository must not be null");
        }
        this.settlementTransactionRepository = settlementTransactionRepository;
        this.settlementPeriodRepository = settlementPeriodRepository;
    }

    public List<SettlementTransaction> execute(UUID periodId, boolean includeDeleted) {
        if (periodId == null) {
            throw new IllegalArgumentException("periodId must not be null");
        }
        if (settlementPeriodRepository.findById(periodId).isEmpty()) {
            throw new IllegalArgumentException("period not found");
        }
        if (includeDeleted) {
            return settlementTransactionRepository.findByPeriodId(periodId);
        }
        return settlementTransactionRepository.findActiveByPeriodId(periodId);
    }
}
