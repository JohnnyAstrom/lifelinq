package app.lifelinq.features.meals.contract;

public final class MealsShoppingDuplicateItemException extends RuntimeException {
    public MealsShoppingDuplicateItemException(String message) {
        super(message);
    }
}
