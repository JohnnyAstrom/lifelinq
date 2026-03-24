package app.lifelinq.features.meals.contract;

public record MealReadinessView(
        String state,
        int coveredIngredientCount,
        int partiallyCoveredIngredientCount,
        int missingIngredientCount,
        int unknownIngredientCount,
        int boughtIngredientCount,
        int toBuyIngredientCount
) {
}
