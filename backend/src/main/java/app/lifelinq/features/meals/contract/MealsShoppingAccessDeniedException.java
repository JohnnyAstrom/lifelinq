package app.lifelinq.features.meals.contract;

public final class MealsShoppingAccessDeniedException extends RuntimeException {
    public MealsShoppingAccessDeniedException(String message) {
        super(message);
    }
}
