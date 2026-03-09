package app.lifelinq.features.economy.infrastructure;

import app.lifelinq.features.economy.domain.SettlementTransaction;

public class SettlementTransactionMapper {
    public SettlementTransactionEntity toEntity(SettlementTransaction transaction) {
        return new SettlementTransactionEntity(
                transaction.getId(),
                transaction.getPeriodId(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getPaidByUserId(),
                transaction.getCreatedAt(),
                transaction.getDeletedAt(),
                transaction.getCategory()
        );
    }

    public SettlementTransaction toDomain(SettlementTransactionEntity entity) {
        return new SettlementTransaction(
                entity.getId(),
                entity.getPeriodId(),
                entity.getAmount(),
                entity.getDescription(),
                entity.getPaidByUserId(),
                entity.getCreatedAt(),
                entity.getDeletedAt(),
                entity.getCategory()
        );
    }
}
