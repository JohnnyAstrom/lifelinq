package app.lifelinq.features.meals.contract;

import app.lifelinq.features.shopping.domain.ShoppingUnit;
import java.math.BigDecimal;

public record IngredientInput(
        String name,
        BigDecimal quantity,
        ShoppingUnit unit,
        Integer position
) {}
