package app.lifelinq.features.meals.contract;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RecipeView(
        UUID recipeId,
        UUID groupId,
        String name,
        String sourceName,
        String sourceUrl,
        String originKind,
        String servings,
        String shortNote,
        String instructions,
        Instant createdAt,
        Instant updatedAt,
        Instant archivedAt,
        Boolean savedInRecipes,
        Boolean deleteEligible,
        String deleteBlockedReason,
        List<IngredientView> ingredients
) {
    public RecipeView(
            UUID recipeId,
            UUID groupId,
            String name,
            String sourceName,
            String sourceUrl,
            String originKind,
            String shortNote,
            String instructions,
            Instant createdAt,
            Instant updatedAt,
            Instant archivedAt,
            Boolean savedInRecipes,
            Boolean deleteEligible,
            String deleteBlockedReason,
            List<IngredientView> ingredients
    ) {
        this(
                recipeId,
                groupId,
                name,
                sourceName,
                sourceUrl,
                originKind,
                null,
                shortNote,
                instructions,
                createdAt,
                updatedAt,
                archivedAt,
                savedInRecipes,
                deleteEligible,
                deleteBlockedReason,
                ingredients
        );
    }
}
