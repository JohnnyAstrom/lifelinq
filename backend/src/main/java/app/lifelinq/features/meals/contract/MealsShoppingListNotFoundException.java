package app.lifelinq.features.meals.contract;

public final class MealsShoppingListNotFoundException extends RuntimeException {
    public MealsShoppingListNotFoundException(String message) {
        super(message);
    }
}
