package app.lifelinq.features.meals.contract;

import java.util.UUID;

public record AddMealOutput(
        UUID weekPlanId,
        int year,
        int isoWeek,
        PlannedMealView meal
) {}
