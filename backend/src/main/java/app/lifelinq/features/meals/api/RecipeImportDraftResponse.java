package app.lifelinq.features.meals.api;

import java.util.List;

public record RecipeImportDraftResponse(
        String name,
        String sourceName,
        String sourceUrl,
        String originKind,
        String servings,
        String shortNote,
        String instructions,
        List<RecipeImportDraftIngredientResponse> ingredients
) {
    public RecipeImportDraftResponse(
            String name,
            String sourceName,
            String sourceUrl,
            String originKind,
            String shortNote,
            String instructions,
            List<RecipeImportDraftIngredientResponse> ingredients
    ) {
        this(name, sourceName, sourceUrl, originKind, null, shortNote, instructions, ingredients);
    }
}
