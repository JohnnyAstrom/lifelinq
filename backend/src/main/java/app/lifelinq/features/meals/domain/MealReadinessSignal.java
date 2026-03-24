package app.lifelinq.features.meals.domain;

public record MealReadinessSignal(
        MealReadinessState state,
        int coveredIngredientCount,
        int partiallyCoveredIngredientCount,
        int missingIngredientCount,
        int unknownIngredientCount,
        int boughtIngredientCount,
        int toBuyIngredientCount
) {
    public MealReadinessSignal {
        if (state == null) {
            throw new IllegalArgumentException("state must not be null");
        }
    }
}
