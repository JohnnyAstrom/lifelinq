package app.lifelinq.features.economy.application;

import app.lifelinq.features.economy.domain.SettlementPeriod;
import app.lifelinq.features.economy.domain.SettlementPeriodRepository;
import app.lifelinq.features.economy.domain.SettlementTransaction;
import app.lifelinq.features.economy.domain.SettlementTransactionRepository;
import java.time.Clock;
import java.util.UUID;

public final class SoftDeleteSettlementTransactionUseCase {
    private final SettlementTransactionRepository settlementTransactionRepository;
    private final SettlementPeriodRepository settlementPeriodRepository;
    private final Clock clock;

    public SoftDeleteSettlementTransactionUseCase(
            SettlementTransactionRepository settlementTransactionRepository,
            SettlementPeriodRepository settlementPeriodRepository,
            Clock clock
    ) {
        if (settlementTransactionRepository == null) {
            throw new IllegalArgumentException("settlementTransactionRepository must not be null");
        }
        if (settlementPeriodRepository == null) {
            throw new IllegalArgumentException("settlementPeriodRepository must not be null");
        }
        if (clock == null) {
            throw new IllegalArgumentException("clock must not be null");
        }
        this.settlementTransactionRepository = settlementTransactionRepository;
        this.settlementPeriodRepository = settlementPeriodRepository;
        this.clock = clock;
    }

    public boolean execute(UUID transactionId) {
        if (transactionId == null) {
            throw new IllegalArgumentException("transactionId must not be null");
        }
        SettlementTransaction transaction = settlementTransactionRepository.findById(transactionId).orElse(null);
        if (transaction == null) {
            return false;
        }
        SettlementPeriod period = settlementPeriodRepository.findById(transaction.getPeriodId())
                .orElseThrow(() -> new IllegalStateException("period not found"));
        SettlementTransaction deleted = transaction.softDelete(clock.instant(), period);
        settlementTransactionRepository.save(deleted);
        return deleted.getDeletedAt() != null;
    }
}
