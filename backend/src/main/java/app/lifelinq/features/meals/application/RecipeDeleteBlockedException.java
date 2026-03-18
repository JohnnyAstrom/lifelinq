package app.lifelinq.features.meals.application;

public final class RecipeDeleteBlockedException extends RuntimeException {
    public RecipeDeleteBlockedException(String message) {
        super(message);
    }
}
