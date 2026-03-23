package app.lifelinq.features.meals.contract;

import java.util.UUID;

public record RecentPlannedMealView(
        int year,
        int isoWeek,
        int dayOfWeek,
        String mealType,
        String mealTitle,
        UUID recipeId,
        String recipeTitle
) {}
