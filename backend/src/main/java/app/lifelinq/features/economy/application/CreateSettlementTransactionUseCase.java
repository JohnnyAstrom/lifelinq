package app.lifelinq.features.economy.application;

import app.lifelinq.features.group.contract.AccessDeniedException;
import app.lifelinq.features.economy.domain.SettlementPeriod;
import app.lifelinq.features.economy.domain.SettlementPeriodRepository;
import app.lifelinq.features.economy.domain.SettlementTransaction;
import app.lifelinq.features.economy.domain.SettlementTransactionRepository;
import java.time.Clock;
import java.util.UUID;

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

    public CreateSettlementTransactionResult execute(UUID activeGroupId, CreateSettlementTransactionCommand command) {
        if (activeGroupId == null) {
            throw new IllegalArgumentException("activeGroupId must not be null");
        }
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        SettlementPeriod period = settlementPeriodRepository.findById(command.periodId())
                .orElseThrow(() -> new PeriodNotFoundException(command.periodId()));
        if (!activeGroupId.equals(period.getGroupId())) {
            throw new AccessDeniedException("period does not belong to active group");
        }
        if (!period.isOpen()) {
            throw new IllegalStateException("period must be OPEN");
        }
        SettlementTransaction transaction = SettlementTransaction.createForPeriod(
                period,
                command.amount(),
                command.description(),
                command.actorUserId(),
                command.paidByUserId(),
                clock.instant(),
                command.category()
        );
        settlementTransactionRepository.save(transaction);
        return new CreateSettlementTransactionResult(transaction);
    }
}
