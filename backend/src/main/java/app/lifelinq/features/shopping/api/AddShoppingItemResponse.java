package app.lifelinq.features.shopping.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AddShoppingItemResponse(
        UUID itemId,
        String name,
        String outcome,
        String status,
        BigDecimal quantity,
        String unit,
        String sourceKind,
        String sourceLabel,
        Instant createdAt,
        Instant boughtAt
) {}
