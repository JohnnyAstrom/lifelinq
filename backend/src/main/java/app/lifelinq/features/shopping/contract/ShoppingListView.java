package app.lifelinq.features.shopping.contract;

import java.util.List;
import java.util.UUID;

public record ShoppingListView(
        UUID id,
        String name,
        String type,
        List<ShoppingItemView> items
) {}
