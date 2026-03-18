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
        String shortNote,
        String instructions,
        Instant createdAt,
        Instant updatedAt,
        Instant archivedAt,
        List<IngredientView> ingredients
) {}
