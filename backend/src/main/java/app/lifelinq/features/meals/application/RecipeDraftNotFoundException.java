package app.lifelinq.features.meals.application;

import java.util.UUID;

public final class RecipeDraftNotFoundException extends RuntimeException {
    public RecipeDraftNotFoundException(UUID draftId) {
        super("Recipe draft not found: " + draftId);
    }
}
