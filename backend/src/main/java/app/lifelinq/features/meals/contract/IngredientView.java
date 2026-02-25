package app.lifelinq.features.meals.contract;

import java.math.BigDecimal;
import java.util.UUID;

public record IngredientView(
        UUID id,
        String name,
        BigDecimal quantity,
        IngredientUnitView unit,
        int position
) {}
