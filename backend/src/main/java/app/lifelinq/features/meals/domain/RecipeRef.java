package app.lifelinq.features.meals.domain;

import java.util.UUID;

public record RecipeRef(UUID recipeId, String title) {
    public RecipeRef {
        if (recipeId == null) {
            throw new IllegalArgumentException("recipeId must not be null");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
    }
}
