package app.lifelinq.features.meals.contract;

import java.util.UUID;

public record PlannedMealView(
        int dayOfWeek,
        UUID recipeId,
        String recipeTitle
) {}
