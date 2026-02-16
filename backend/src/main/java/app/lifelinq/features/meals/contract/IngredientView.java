package app.lifelinq.features.meals.contract;

import app.lifelinq.features.shopping.contract.ShoppingUnitView;
import java.math.BigDecimal;
import java.util.UUID;

public record IngredientView(
        UUID id,
        String name,
        BigDecimal quantity,
        ShoppingUnitView unit,
        int position
) {}
