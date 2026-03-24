package app.lifelinq.features.meals.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record MealIngredientNeed(
        UUID ingredientId,
        int position,
        String ingredientName,
        String normalizedShoppingName,
        String rawText,
        BigDecimal quantity,
        String unitName
) {
    public MealIngredientNeed {
        if (ingredientId == null) {
            throw new IllegalArgumentException("ingredientId must not be null");
        }
        if (position < 1) {
            throw new IllegalArgumentException("position must be >= 1");
        }
        if (ingredientName == null || ingredientName.isBlank()) {
            throw new IllegalArgumentException("ingredientName must not be blank");
        }
        if (normalizedShoppingName == null || normalizedShoppingName.isBlank()) {
            throw new IllegalArgumentException("normalizedShoppingName must not be blank");
        }
        if ((quantity == null) != (unitName == null)) {
            throw new IllegalArgumentException("quantity and unitName must be set together");
        }
    }
}
