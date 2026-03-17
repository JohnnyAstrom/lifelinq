package app.lifelinq.features.meals.api;

import app.lifelinq.features.meals.contract.IngredientUnitView;
import java.math.BigDecimal;

public record RecipeImportDraftIngredientResponse(
        String name,
        BigDecimal quantity,
        IngredientUnitView unit,
        int position
) {}
