package app.lifelinq.features.meals.application;

public final class RecipeDuplicateAttentionRequiredException extends RuntimeException {
    public RecipeDuplicateAttentionRequiredException(String message) {
        super(message);
    }
}
