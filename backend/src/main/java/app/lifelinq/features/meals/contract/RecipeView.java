package app.lifelinq.features.meals.contract;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RecipeView(
        UUID recipeId,
        UUID groupId,
        String name,
        Instant createdAt,
        List<IngredientView> ingredients
) {}
