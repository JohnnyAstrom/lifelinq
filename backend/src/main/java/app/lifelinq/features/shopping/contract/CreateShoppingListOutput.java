package app.lifelinq.features.shopping.contract;

import java.util.UUID;

public record CreateShoppingListOutput(
        UUID listId,
        String name
) {}
