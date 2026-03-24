package app.lifelinq.features.meals.contract;

import java.math.BigDecimal;

public record IngredientCoverageView(
        MealIngredientNeedView need,
        String coverageState,
        String shoppingState,
        int matchingItemCount,
        BigDecimal coveredQuantity,
        BigDecimal uncoveredQuantity,
        String uncertaintyReason
) {
}
