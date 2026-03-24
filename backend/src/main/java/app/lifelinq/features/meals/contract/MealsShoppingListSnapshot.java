package app.lifelinq.features.meals.contract;

import java.util.List;
import java.util.UUID;

public record MealsShoppingListSnapshot(
        UUID listId,
        String listName,
        String listType,
        List<MealsShoppingItemSnapshot> items
) {
}
