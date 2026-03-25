package app.lifelinq.features.meals.contract;

import java.math.BigDecimal;
import java.util.List;

public record AggregatedIngredientNeedView(
        String lineId,
        String ingredientName,
        String normalizedShoppingName,
        BigDecimal totalQuantity,
        String unitName,
        List<ContributorMealReferenceView> contributors
) {
}
