package app.lifelinq.features.meals.contract;

import java.time.LocalDate;
import java.util.UUID;

public record RecentMealOccurrenceView(
        UUID weekPlanId,
        int year,
        int isoWeek,
        int dayOfWeek,
        String mealType,
        LocalDate plannedDate,
        String mealTitle,
        String mealIdentityKey,
        String mealIdentityKind,
        UUID recipeId,
        String recipeTitle
) {
}
