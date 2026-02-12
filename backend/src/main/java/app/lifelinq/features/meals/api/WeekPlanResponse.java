package app.lifelinq.features.meals.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WeekPlanResponse(
        UUID weekPlanId,
        int year,
        int isoWeek,
        Instant createdAt,
        List<PlannedMealResponse> meals
) {
}
