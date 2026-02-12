package app.lifelinq.features.shopping.contract;

import java.util.List;
import java.util.UUID;

public record ShoppingListView(
        UUID id,
        String name,
        List<ShoppingItemView> items
) {}
