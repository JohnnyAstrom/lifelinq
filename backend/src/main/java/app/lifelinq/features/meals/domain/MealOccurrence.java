package app.lifelinq.features.meals.domain;

import java.time.LocalDate;
import java.util.UUID;

public record MealOccurrence(
        UUID weekPlanId,
        int year,
        int isoWeek,
        int dayOfWeek,
        MealType mealType,
        LocalDate plannedDate,
        String mealTitle,
        UUID recipeId,
        String recipeTitleSnapshot
) {
    public MealOccurrence {
        if (weekPlanId == null) {
            throw new IllegalArgumentException("weekPlanId must not be null");
        }
        if (isoWeek < 1 || isoWeek > 53) {
            throw new IllegalArgumentException("isoWeek must be between 1 and 53");
        }
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new IllegalArgumentException("dayOfWeek must be between 1 and 7");
        }
        if (mealType == null) {
            throw new IllegalArgumentException("mealType must not be null");
        }
        if (plannedDate == null) {
            throw new IllegalArgumentException("plannedDate must not be null");
        }
        if (mealTitle == null || mealTitle.isBlank()) {
            throw new IllegalArgumentException("mealTitle must not be blank");
        }
        mealTitle = mealTitle.trim();
        recipeTitleSnapshot = normalizeOptional(recipeTitleSnapshot);
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
