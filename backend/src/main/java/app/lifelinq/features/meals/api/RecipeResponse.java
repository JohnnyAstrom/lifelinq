package app.lifelinq.features.meals.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RecipeResponse(
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
        List<IngredientResponse> ingredients
) {}
