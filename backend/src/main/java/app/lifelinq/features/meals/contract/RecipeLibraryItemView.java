package app.lifelinq.features.meals.contract;

import java.time.Instant;
import java.util.UUID;

public record RecipeLibraryItemView(
        UUID recipeId,
        String name,
        RecipeSourceView source,
        RecipeLifecycleView lifecycle,
        Instant makeSoonAt,
        Instant updatedAt,
        int ingredientCount
) {
}
