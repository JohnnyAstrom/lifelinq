package app.lifelinq.features.meals.api;

import app.lifelinq.features.meals.contract.IngredientUnitView;
import java.math.BigDecimal;
import java.util.UUID;

public record IngredientResponse(
        UUID id,
        String name,
        String rawText,
        BigDecimal quantity,
        IngredientUnitView unit,
        int position
) {
    public IngredientResponse(
            UUID id,
            String name,
            BigDecimal quantity,
            IngredientUnitView unit,
            int position
    ) {
        this(id, name, null, quantity, unit, position);
    }
}
