package app.lifelinq.features.meals.application;

import java.util.UUID;

public final class RecipeNotFoundException extends RuntimeException {
    public RecipeNotFoundException(UUID recipeId) {
        super("Recipe not found: " + recipeId);
    }
}
