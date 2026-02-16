package app.lifelinq.features.meals.api;

import app.lifelinq.features.shopping.contract.ShoppingUnitView;
import java.math.BigDecimal;
import java.util.UUID;

public record IngredientResponse(
        UUID id,
        String name,
        BigDecimal quantity,
        ShoppingUnitView unit,
        int position
) {}
