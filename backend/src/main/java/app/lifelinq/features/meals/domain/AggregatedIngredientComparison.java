package app.lifelinq.features.meals.domain;

import java.math.BigDecimal;

public record AggregatedIngredientComparison(
        AggregatedIngredientNeed need,
        AggregatedIngredientComparisonState state,
        BigDecimal quantityOnList,
        BigDecimal remainingQuantity
) {
    public AggregatedIngredientComparison {
        if (need == null) {
            throw new IllegalArgumentException("need must not be null");
        }
        if (state == null) {
            throw new IllegalArgumentException("state must not be null");
        }
        if (state == AggregatedIngredientComparisonState.ALREADY_ON_LIST && remainingQuantity != null) {
            throw new IllegalArgumentException("remainingQuantity must be null for already-on-list lines");
        }
        if (state == AggregatedIngredientComparisonState.ADD_TO_LIST
                && need.totalQuantity() != null
                && remainingQuantity == null) {
            throw new IllegalArgumentException("remainingQuantity must not be null for quantified add-to-list lines");
        }
    }
}
