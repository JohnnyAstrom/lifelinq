package app.lifelinq.features.economy.application;

import app.lifelinq.features.economy.domain.SettlementPeriod;
import app.lifelinq.features.economy.domain.SettlementPeriodRepository;
import app.lifelinq.features.economy.domain.SettlementTransaction;
import app.lifelinq.features.economy.domain.SettlementTransactionRepository;
import java.time.Clock;

public final class CreateSettlementTransactionUseCase {
    private final SettlementPeriodRepository settlementPeriodRepository;
    private final SettlementTransactionRepository settlementTransactionRepository;
    private final Clock clock;

    public CreateSettlementTransactionUseCase(
            SettlementPeriodRepository settlementPeriodRepository,
            SettlementTransactionRepository settlementTransactionRepository,
            Clock clock
    ) {
        if (settlementPeriodRepository == null) {
            throw new IllegalArgumentException("settlementPeriodRepository must not be null");
        }
        if (settlementTransactionRepository == null) {
            throw new IllegalArgumentException("settlementTransactionRepository must not be null");
        }
        if (clock == null) {
            throw new IllegalArgumentException("clock must not be null");
        }
        this.settlementPeriodRepository = settlementPeriodRepository;
        this.settlementTransactionRepository = settlementTransactionRepository;
        this.clock = clock;
    }

    public CreateSettlementTransactionResult execute(CreateSettlementTransactionCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        SettlementPeriod period = settlementPeriodRepository.findById(command.periodId()).orElse(null);
        if (period == null) {
            throw new IllegalArgumentException("period not found");
        }
        if (!period.isOpen()) {
            throw new IllegalStateException("period must be OPEN");
        }
        SettlementTransaction transaction = SettlementTransaction.createForPeriod(
                period,
                command.amount(),
                command.description(),
                command.paidByUserId(),
                clock.instant(),
                command.category()
        );
        settlementTransactionRepository.save(transaction);
        return new CreateSettlementTransactionResult(transaction.getId());
    }
}
