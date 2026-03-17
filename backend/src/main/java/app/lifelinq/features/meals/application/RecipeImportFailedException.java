package app.lifelinq.features.meals.application;

public final class RecipeImportFailedException extends RuntimeException {
    public RecipeImportFailedException(String message) {
        super(message);
    }

    public RecipeImportFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
