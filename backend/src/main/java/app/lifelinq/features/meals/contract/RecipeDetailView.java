package app.lifelinq.features.meals.contract;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RecipeDetailView(
        UUID recipeId,
        UUID groupId,
        String name,
        RecipeSourceView source,
        RecipeProvenanceView provenance,
        RecipeLifecycleView lifecycle,
        String servings,
        Instant makeSoonAt,
        String shortNote,
        String instructions,
        Instant createdAt,
        Instant updatedAt,
        boolean savedInRecipes,
        List<IngredientView> ingredients
) {
}
