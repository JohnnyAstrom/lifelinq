package app.lifelinq.features.meals.contract;

import java.time.LocalDate;
import java.util.UUID;

public record RecipeUsageSummaryView(
        UUID recipeId,
        String recipeTitle,
        LocalDate lastUsedDate,
        int totalUses,
        int recentUses,
        int distinctWeeks,
        boolean frequent,
        boolean familiar,
        boolean makeSoon,
        boolean preferenceFit,
        boolean deprioritized
) {
}
