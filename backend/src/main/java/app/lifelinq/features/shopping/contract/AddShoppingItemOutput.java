package app.lifelinq.features.shopping.contract;

import java.time.Instant;
import java.util.UUID;

public record AddShoppingItemOutput(
        UUID listId,
        UUID itemId,
        String name,
        ShoppingItemStatusView status,
        Instant createdAt,
        Instant boughtAt
) {}
