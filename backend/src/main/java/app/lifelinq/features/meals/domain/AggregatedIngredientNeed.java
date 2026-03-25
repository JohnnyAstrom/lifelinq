package app.lifelinq.features.meals.domain;

import java.math.BigDecimal;
import java.util.List;

public record AggregatedIngredientNeed(
        String lineId,
        String ingredientName,
        String normalizedShoppingName,
        BigDecimal totalQuantity,
        String unitName,
        List<WeekShoppingContributorMeal> contributors
) {
    public AggregatedIngredientNeed {
        if (lineId == null || lineId.isBlank()) {
            throw new IllegalArgumentException("lineId must not be blank");
        }
        if (ingredientName == null || ingredientName.isBlank()) {
            throw new IllegalArgumentException("ingredientName must not be blank");
        }
        if (normalizedShoppingName == null || normalizedShoppingName.isBlank()) {
            throw new IllegalArgumentException("normalizedShoppingName must not be blank");
        }
        if ((totalQuantity == null) != (unitName == null)) {
            throw new IllegalArgumentException("totalQuantity and unitName must be set together");
        }
        if (contributors == null) {
            throw new IllegalArgumentException("contributors must not be null");
        }
        contributors = List.copyOf(contributors);
    }
}
