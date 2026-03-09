package app.lifelinq.features.economy.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record RecommendedPayment(UUID fromUserId, UUID toUserId, BigDecimal amount) {
}
