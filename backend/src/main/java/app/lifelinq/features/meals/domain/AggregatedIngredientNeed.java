package app.lifelinq.features.meals.domain;

import java.math.BigDecimal;
import java.util.List;

public record AggregatedIngredientNeed(
        String lineId,
        String ingredientName,
        String normalizedShoppingName,
        BigDecimal totalQuantity,
        String unitName,
        AggregatedIngredientQuantityConfidence quantityConfidence,
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
        if (quantityConfidence == null) {
            throw new IllegalArgumentException("quantityConfidence must not be null");
        }
        if (quantityConfidence == AggregatedIngredientQuantityConfidence.EXACT) {
            if ((totalQuantity == null) != (unitName == null) || totalQuantity == null) {
                throw new IllegalArgumentException("exact aggregated quantities must include totalQuantity and unitName");
            }
        } else if (totalQuantity != null || unitName != null) {
            throw new IllegalArgumentException("non-exact aggregated quantities must not preserve totalQuantity or unitName");
        }
        if (contributors == null) {
            throw new IllegalArgumentException("contributors must not be null");
        }
        contributors = List.copyOf(contributors);
    }
}
