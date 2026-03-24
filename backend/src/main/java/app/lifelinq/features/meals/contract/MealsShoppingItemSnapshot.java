package app.lifelinq.features.meals.contract;

import java.math.BigDecimal;
import java.util.UUID;

public record MealsShoppingItemSnapshot(
        UUID itemId,
        String name,
        String status,
        BigDecimal quantity,
        String unitName,
        String sourceKind,
        String sourceLabel
) {
}
