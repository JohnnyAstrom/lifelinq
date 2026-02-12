package app.lifelinq.features.meals.api;

import java.util.UUID;

public record PlannedMealResponse(
        int dayOfWeek,
        UUID recipeId,
        String recipeTitle
) {
}
