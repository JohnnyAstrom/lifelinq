package app.lifelinq.features.meals.contract;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WeekPlanView(
        UUID weekPlanId,
        int year,
        int isoWeek,
        Instant createdAt,
        List<PlannedMealView> meals
) {}
