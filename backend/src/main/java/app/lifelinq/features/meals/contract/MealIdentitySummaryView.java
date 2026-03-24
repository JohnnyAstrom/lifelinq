package app.lifelinq.features.meals.contract;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record MealIdentitySummaryView(
        String mealIdentityKey,
        String mealIdentityKind,
        String title,
        UUID recipeId,
        LocalDate lastPlannedDate,
        int totalOccurrences,
        int recentOccurrences,
        int distinctWeeks,
        Set<String> usedMealTypes,
        boolean recent,
        boolean frequent,
        boolean familiar,
        boolean fallback,
        boolean preferenceFit,
        boolean deprioritized,
        boolean makeSoon
) {
}
