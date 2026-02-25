package app.lifelinq.features.meals.application;

public final class MealsShoppingListNotFoundException extends RuntimeException {
    public MealsShoppingListNotFoundException(String message) {
        super(message);
    }
}
