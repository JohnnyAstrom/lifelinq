package app.lifelinq.features.meals.domain;

import java.util.UUID;

public record RecentPlannedMeal(
        int year,
        int isoWeek,
        int dayOfWeek,
        MealType mealType,
        String mealTitle,
        UUID recipeId,
        String recipeTitleSnapshot
) {}
