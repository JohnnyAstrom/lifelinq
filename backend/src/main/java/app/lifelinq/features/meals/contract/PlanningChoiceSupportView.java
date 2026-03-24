package app.lifelinq.features.meals.contract;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PlanningChoiceSupportView(
        String scenario,
        LocalDate referenceDate,
        Integer year,
        Integer isoWeek,
        Integer dayOfWeek,
        String mealType,
        UUID recipeId,
        List<MealChoiceCandidateView> recentCandidates,
        List<MealChoiceCandidateView> familiarCandidates,
        List<MealChoiceCandidateView> fallbackCandidates,
        List<MealChoiceCandidateView> makeSoonCandidates
) {
}
