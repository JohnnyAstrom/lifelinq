package app.lifelinq.features.meals.contract;

import java.math.BigDecimal;

public record RecipeImportDraftIngredientView(
        String name,
        BigDecimal quantity,
        IngredientUnitView unit,
        int position
) {}
