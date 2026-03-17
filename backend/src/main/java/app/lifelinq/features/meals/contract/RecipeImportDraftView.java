package app.lifelinq.features.meals.contract;

import java.util.List;

public record RecipeImportDraftView(
        String name,
        String sourceName,
        String sourceUrl,
        String originKind,
        String shortNote,
        String instructions,
        List<RecipeImportDraftIngredientView> ingredients
) {}
