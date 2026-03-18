package app.lifelinq.features.meals.contract;

import app.lifelinq.features.meals.domain.IngredientUnit;
import java.math.BigDecimal;

public record IngredientInput(
        String name,
        String rawText,
        BigDecimal quantity,
        IngredientUnit unit,
        Integer position
) {
    public IngredientInput(
            String name,
            BigDecimal quantity,
            IngredientUnit unit,
            Integer position
    ) {
        this(name, null, quantity, unit, position);
    }
}
