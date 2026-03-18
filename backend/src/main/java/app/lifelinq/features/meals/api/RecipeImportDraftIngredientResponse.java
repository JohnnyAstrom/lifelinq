package app.lifelinq.features.meals.api;

import app.lifelinq.features.meals.contract.IngredientUnitView;
import java.math.BigDecimal;

public record RecipeImportDraftIngredientResponse(
        String name,
        String rawText,
        BigDecimal quantity,
        IngredientUnitView unit,
        int position
) {
    public RecipeImportDraftIngredientResponse(
            String name,
            BigDecimal quantity,
            IngredientUnitView unit,
            int position
    ) {
        this(name, null, quantity, unit, position);
    }
}
