package app.lifelinq.features.meals.application;

public class MealsAccessDeniedException extends RuntimeException {
    public MealsAccessDeniedException(String message) {
        super(message);
    }
}
