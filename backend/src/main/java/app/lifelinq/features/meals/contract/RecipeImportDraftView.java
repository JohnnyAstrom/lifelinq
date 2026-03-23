package app.lifelinq.features.meals.contract;

import java.util.List;

public record RecipeImportDraftView(
        String name,
        String sourceName,
        String sourceUrl,
        String originKind,
        String servings,
        String shortNote,
        String instructions,
        List<RecipeImportDraftIngredientView> ingredients
) {
    public RecipeImportDraftView(
            String name,
            String sourceName,
            String sourceUrl,
            String originKind,
            String shortNote,
            String instructions,
            List<RecipeImportDraftIngredientView> ingredients
    ) {
        this(name, sourceName, sourceUrl, originKind, null, shortNote, instructions, ingredients);
    }
}
