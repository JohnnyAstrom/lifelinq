package app.lifelinq.features.meals.api;

import java.util.UUID;

public record AddMealResponse(
        UUID weekPlanId,
        int year,
        int isoWeek,
        PlannedMealResponse meal
) {
}
