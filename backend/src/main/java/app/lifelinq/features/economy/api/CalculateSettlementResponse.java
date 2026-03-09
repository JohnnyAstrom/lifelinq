package app.lifelinq.features.economy.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CalculateSettlementResponse(
        List<BalanceItem> balances,
        List<PaymentItem> recommendedPayments
) {
    public record BalanceItem(UUID userId, BigDecimal amount) {
    }

    public record PaymentItem(UUID fromUserId, UUID toUserId, BigDecimal amount) {
    }
}
