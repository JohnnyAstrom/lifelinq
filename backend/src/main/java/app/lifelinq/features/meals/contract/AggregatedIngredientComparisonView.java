package app.lifelinq.features.meals.contract;

import java.math.BigDecimal;

public record AggregatedIngredientComparisonView(
        AggregatedIngredientNeedView need,
        String comparisonState,
        BigDecimal quantityOnList,
        BigDecimal remainingQuantity
) {
}
