package app.lifelinq.features.meals.domain;

import java.util.UUID;

public record RecipeDuplicateAssessment(
        boolean attentionRequired,
        RecipeDuplicateMatchType matchType,
        UUID matchingRecipeId,
        String reason
) {
    public static RecipeDuplicateAssessment clear() {
        return new RecipeDuplicateAssessment(false, null, null, null);
    }
}
