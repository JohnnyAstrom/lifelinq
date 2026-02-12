package app.lifelinq.features.shopping.contract;

import java.time.Instant;
import java.util.UUID;

public record ShoppingItemView(
        UUID id,
        String name,
        ShoppingItemStatusView status,
        Instant createdAt,
        Instant boughtAt
) {}
