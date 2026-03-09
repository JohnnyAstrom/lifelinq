package app.lifelinq.features.economy.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SettlementTransactionResponse(
        UUID transactionId,
        UUID periodId,
        BigDecimal amount,
        String description,
        UUID createdByUserId,
        UUID paidByUserId,
        Instant createdAt,
        Instant deletedAt,
        String category
) {
}
