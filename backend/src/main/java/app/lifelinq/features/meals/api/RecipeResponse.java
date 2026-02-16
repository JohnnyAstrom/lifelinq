package app.lifelinq.features.meals.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RecipeResponse(
        UUID recipeId,
        UUID householdId,
        String name,
        Instant createdAt,
        List<IngredientResponse> ingredients
) {}
