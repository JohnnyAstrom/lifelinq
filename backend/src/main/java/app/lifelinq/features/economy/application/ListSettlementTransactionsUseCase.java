package app.lifelinq.features.economy.application;

import app.lifelinq.features.group.contract.AccessDeniedException;
import app.lifelinq.features.economy.domain.SettlementPeriodRepository;
import app.lifelinq.features.economy.domain.SettlementPeriod;
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

    public List<SettlementTransaction> execute(UUID activeGroupId, UUID periodId, boolean includeDeleted) {
        if (activeGroupId == null) {
            throw new IllegalArgumentException("activeGroupId must not be null");
        }
        if (periodId == null) {
            throw new IllegalArgumentException("periodId must not be null");
        }
        SettlementPeriod period = settlementPeriodRepository.findById(periodId)
                .orElseThrow(() -> new PeriodNotFoundException(periodId));
        if (!activeGroupId.equals(period.getGroupId())) {
            throw new AccessDeniedException("period does not belong to active group");
        }
        if (includeDeleted) {
            return settlementTransactionRepository.findByPeriodId(periodId);
        }
        return settlementTransactionRepository.findActiveByPeriodId(periodId);
    }
}
