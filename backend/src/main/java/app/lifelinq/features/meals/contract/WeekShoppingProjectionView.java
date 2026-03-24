package app.lifelinq.features.meals.contract;

import java.util.List;
import java.util.UUID;

public record WeekShoppingProjectionView(
        UUID weekPlanId,
        int year,
        int isoWeek,
        int mealsNeedingShoppingCount,
        int partiallyReadyMealCount,
        int readyFromShoppingViewMealCount,
        int readinessUnclearMealCount,
        ShoppingDeltaView delta,
        List<MealShoppingProjectionView> meals
) {
}
