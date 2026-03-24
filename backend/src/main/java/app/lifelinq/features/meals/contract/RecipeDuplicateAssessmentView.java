package app.lifelinq.features.meals.contract;

public record RecipeDuplicateAssessmentView(
        boolean attentionRequired,
        String matchType,
        String reason,
        RecipeIdentitySummaryView matchingRecipe
) {
}
