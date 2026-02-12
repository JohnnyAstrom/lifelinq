package app.lifelinq.features.shopping.api;

import java.time.Instant;
import java.util.UUID;

public record ShoppingItemResponse(
        UUID id,
        String name,
        String status,
        Instant createdAt,
        Instant boughtAt
) {}
