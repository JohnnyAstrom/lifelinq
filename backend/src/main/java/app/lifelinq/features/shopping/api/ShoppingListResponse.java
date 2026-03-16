package app.lifelinq.features.shopping.api;

import java.util.List;
import java.util.UUID;

public record ShoppingListResponse(
        UUID id,
        String name,
        String type,
        List<ShoppingItemResponse> items
) {}
