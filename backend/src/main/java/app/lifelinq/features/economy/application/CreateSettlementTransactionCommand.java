package app.lifelinq.features.economy.application;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateSettlementTransactionCommand(
        UUID periodId,
        BigDecimal amount,
        String description,
        UUID actorUserId,
        UUID paidByUserId,
        String category
) {
}
