package app.lifelinq.features.meals.contract;

import java.util.UUID;

public record RecipeIdentitySummaryView(
        UUID recipeId,
        String name,
        RecipeSourceView source,
        RecipeLifecycleView lifecycle
) {
}
