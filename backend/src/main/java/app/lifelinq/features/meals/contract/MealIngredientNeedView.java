package app.lifelinq.features.meals.contract;

import java.math.BigDecimal;
import java.util.UUID;

public record MealIngredientNeedView(
        UUID ingredientId,
        int position,
        String ingredientName,
        String normalizedShoppingName,
        String rawText,
        BigDecimal quantity,
        String unitName
) {
}
