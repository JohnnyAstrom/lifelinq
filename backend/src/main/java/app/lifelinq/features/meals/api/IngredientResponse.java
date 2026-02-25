package app.lifelinq.features.meals.api;

import app.lifelinq.features.meals.contract.IngredientUnitView;
import java.math.BigDecimal;
import java.util.UUID;

public record IngredientResponse(
        UUID id,
        String name,
        BigDecimal quantity,
        IngredientUnitView unit,
        int position
) {}
