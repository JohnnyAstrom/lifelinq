package app.lifelinq.features.shopping.api;

import java.util.UUID;

public record CreateShoppingListResponse(
        UUID listId,
        String name
) {}
