package app.lifelinq.features.meals.application;

public final class MealNotFoundException extends RuntimeException {
    public MealNotFoundException(String message) {
        super(message);
    }
}
