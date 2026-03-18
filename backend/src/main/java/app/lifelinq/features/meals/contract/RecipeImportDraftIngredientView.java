package app.lifelinq.features.meals.contract;

import java.math.BigDecimal;

public record RecipeImportDraftIngredientView(
        String name,
        String rawText,
        BigDecimal quantity,
        IngredientUnitView unit,
        int position
) {
    public RecipeImportDraftIngredientView(
            String name,
            BigDecimal quantity,
            IngredientUnitView unit,
            int position
    ) {
        this(name, null, quantity, unit, position);
    }
}
