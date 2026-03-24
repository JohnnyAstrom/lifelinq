package app.lifelinq.features.meals.domain;

import java.time.LocalDate;
import java.util.UUID;

public record PlanningContext(
        PlanningScenario scenario,
        LocalDate referenceDate,
        Integer year,
        Integer isoWeek,
        Integer dayOfWeek,
        MealType mealType,
        UUID recipeId
) {
    public PlanningContext {
        if (scenario == null) {
            throw new IllegalArgumentException("scenario must not be null");
        }
        if (referenceDate == null) {
            throw new IllegalArgumentException("referenceDate must not be null");
        }
        if (dayOfWeek != null && (dayOfWeek < 1 || dayOfWeek > 7)) {
            throw new IllegalArgumentException("dayOfWeek must be between 1 and 7");
        }
        switch (scenario) {
            case SLOT -> {
                if (year == null || isoWeek == null || dayOfWeek == null || mealType == null) {
                    throw new IllegalArgumentException("slot planning context requires year, isoWeek, dayOfWeek, and mealType");
                }
            }
            case TONIGHT -> {
                if (mealType == null) {
                    throw new IllegalArgumentException("tonight planning context requires mealType");
                }
            }
            case WEEK_START -> {
                if (year == null || isoWeek == null) {
                    throw new IllegalArgumentException("week-start planning context requires year and isoWeek");
                }
            }
            case RECIPE_CONTEXT -> {
                if (recipeId == null) {
                    throw new IllegalArgumentException("recipe context requires recipeId");
                }
            }
        }
    }
}
