package app.lifelinq.features.meals.domain;

import java.util.List;

public record ShoppingDelta(List<IngredientCoverage> unresolvedIngredients) {
    public ShoppingDelta {
        if (unresolvedIngredients == null) {
            throw new IllegalArgumentException("unresolvedIngredients must not be null");
        }
        unresolvedIngredients = List.copyOf(unresolvedIngredients);
    }
}
