package app.lifelinq.features.meals.api;

import java.util.UUID;

public record PlannedMealResponse(
        int dayOfWeek,
        String mealType,
        UUID recipeId,
        String recipeTitle
) {
}
