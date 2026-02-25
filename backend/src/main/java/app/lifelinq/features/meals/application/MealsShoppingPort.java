package app.lifelinq.features.meals.application;

import app.lifelinq.features.meals.domain.IngredientUnit;
import java.math.BigDecimal;
import java.util.UUID;

public interface MealsShoppingPort {
    void addShoppingItem(
            UUID householdId,
            UUID actorUserId,
            UUID listId,
            String itemName,
            BigDecimal quantity,
            IngredientUnit unit
    );
}
