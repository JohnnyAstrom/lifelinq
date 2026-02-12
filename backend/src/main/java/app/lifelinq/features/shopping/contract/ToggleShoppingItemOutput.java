package app.lifelinq.features.shopping.contract;

import java.time.Instant;
import java.util.UUID;

public record ToggleShoppingItemOutput(
        UUID listId,
        UUID itemId,
        ShoppingItemStatusView status,
        Instant boughtAt
) {}
