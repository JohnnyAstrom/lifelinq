package app.lifelinq.features.meals.contract;

public record RecipeLifecycleView(
        String state,
        boolean deleteEligible,
        String deleteBlockedReason
) {
}
