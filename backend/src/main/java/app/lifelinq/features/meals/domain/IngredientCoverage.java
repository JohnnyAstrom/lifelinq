package app.lifelinq.features.meals.domain;

import java.math.BigDecimal;

public record IngredientCoverage(
        MealIngredientNeed need,
        IngredientCoverageState state,
        ShoppingCoverageState shoppingState,
        int matchingItemCount,
        BigDecimal coveredQuantity,
        BigDecimal uncoveredQuantity,
        String uncertaintyReason
) {
    public IngredientCoverage {
        if (need == null) {
            throw new IllegalArgumentException("need must not be null");
        }
        if (state == null) {
            throw new IllegalArgumentException("state must not be null");
        }
        if (shoppingState == null) {
            throw new IllegalArgumentException("shoppingState must not be null");
        }
        if (matchingItemCount < 0) {
            throw new IllegalArgumentException("matchingItemCount must be >= 0");
        }
    }
}
