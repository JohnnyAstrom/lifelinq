package app.lifelinq.features.meals.api;

import java.util.UUID;

public record RecentPlannedMealResponse(
        int year,
        int isoWeek,
        int dayOfWeek,
        String mealType,
        String mealTitle,
        UUID recipeId,
        String recipeTitle
) {}
