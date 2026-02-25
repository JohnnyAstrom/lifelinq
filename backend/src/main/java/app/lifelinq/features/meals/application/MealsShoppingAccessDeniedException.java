package app.lifelinq.features.meals.application;

public final class MealsShoppingAccessDeniedException extends RuntimeException {
    public MealsShoppingAccessDeniedException(String message) {
        super(message);
    }
}
