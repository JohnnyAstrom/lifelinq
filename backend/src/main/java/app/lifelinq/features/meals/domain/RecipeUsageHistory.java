package app.lifelinq.features.meals.domain;

import java.time.LocalDate;
import java.util.UUID;

public record RecipeUsageHistory(
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
    public RecipeUsageHistory {
        if (recipeId == null) {
            throw new IllegalArgumentException("recipeId must not be null");
        }
        if (recipeTitle == null || recipeTitle.isBlank()) {
            throw new IllegalArgumentException("recipeTitle must not be blank");
        }
        if (lastUsedDate == null) {
            throw new IllegalArgumentException("lastUsedDate must not be null");
        }
        if (totalUses < 0 || recentUses < 0 || distinctWeeks < 0) {
            throw new IllegalArgumentException("usage counts must not be negative");
        }
        recipeTitle = recipeTitle.trim();
    }
}
