package app.lifelinq.features.meals.domain;

import java.util.List;
import java.util.UUID;

public record WeekShoppingProjection(
        UUID weekPlanId,
        int year,
        int isoWeek,
        int mealsNeedingShoppingCount,
        int partiallyReadyMealCount,
        int readyFromShoppingViewMealCount,
        int readinessUnclearMealCount,
        ShoppingDelta delta,
        List<MealShoppingProjection> meals
) {
    public WeekShoppingProjection {
        if (delta == null) {
            throw new IllegalArgumentException("delta must not be null");
        }
        if (meals == null) {
            throw new IllegalArgumentException("meals must not be null");
        }
        meals = List.copyOf(meals);
    }
}
