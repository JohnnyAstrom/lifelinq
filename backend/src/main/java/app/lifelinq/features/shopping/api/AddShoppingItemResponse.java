package app.lifelinq.features.shopping.api;

import java.time.Instant;
import java.util.UUID;

public record AddShoppingItemResponse(
        UUID itemId,
        String name,
        String status,
        Instant createdAt,
        Instant boughtAt
) {}
