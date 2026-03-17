package app.lifelinq.features.meals.api;

import java.util.List;

public record RecipeImportDraftResponse(
        String name,
        String sourceName,
        String sourceUrl,
        String originKind,
        String shortNote,
        String instructions,
        List<RecipeImportDraftIngredientResponse> ingredients
) {}
