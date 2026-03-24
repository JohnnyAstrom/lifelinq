package app.lifelinq.features.meals.domain;

import java.util.List;
import java.util.UUID;

public record MealShoppingProjection(
        int year,
        int isoWeek,
        int dayOfWeek,
        MealType mealType,
        String mealTitle,
        UUID recipeId,
        String recipeTitle,
        ShoppingLinkReference shoppingLink,
        List<IngredientCoverage> ingredientCoverage,
        ShoppingDelta delta,
        MealReadinessSignal readiness
) {
    public MealShoppingProjection {
        if (mealType == null) {
            throw new IllegalArgumentException("mealType must not be null");
        }
        if (mealTitle == null || mealTitle.isBlank()) {
            throw new IllegalArgumentException("mealTitle must not be blank");
        }
        if (shoppingLink == null) {
            throw new IllegalArgumentException("shoppingLink must not be null");
        }
        if (ingredientCoverage == null) {
            throw new IllegalArgumentException("ingredientCoverage must not be null");
        }
        if (delta == null) {
            throw new IllegalArgumentException("delta must not be null");
        }
        if (readiness == null) {
            throw new IllegalArgumentException("readiness must not be null");
        }
        ingredientCoverage = List.copyOf(ingredientCoverage);
    }

    public boolean recipeBacked() {
        return recipeId != null;
    }
}
