package app.lifelinq.features.meals.contract;

import java.time.LocalDate;
import java.util.UUID;

public record MealChoiceCandidateView(
        String family,
        String mealIdentityKey,
        String mealIdentityKind,
        String title,
        UUID recipeId,
        LocalDate lastPlannedDate,
        int totalOccurrences,
        boolean recent,
        boolean frequent,
        boolean familiar,
        boolean fallback,
        boolean slotFit,
        boolean preferenceFit,
        boolean deprioritized,
        boolean makeSoon,
        String surfacedBecause
) {
}
