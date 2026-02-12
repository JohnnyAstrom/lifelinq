package app.lifelinq.features.shopping.api;

import java.time.Instant;
import java.util.UUID;

public record ToggleShoppingItemResponse(
        UUID itemId,
        String status,
        Instant boughtAt
) {}
