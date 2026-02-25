package app.lifelinq.features.meals.api;

import app.lifelinq.features.household.application.AccessDeniedException;
import app.lifelinq.features.meals.application.MealNotFoundException;
import app.lifelinq.features.meals.application.MealsShoppingAccessDeniedException;
import app.lifelinq.features.meals.application.MealsShoppingDuplicateItemException;
import app.lifelinq.features.meals.application.MealsShoppingListNotFoundException;
import app.lifelinq.features.meals.application.RecipeNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "app.lifelinq.features.meals.api")
public final class MealsExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
    }

    @ExceptionHandler(MealsShoppingAccessDeniedException.class)
    public ResponseEntity<String> handleShoppingAccessDenied(
            MealsShoppingAccessDeniedException ex
    ) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
    }

    @ExceptionHandler(MealNotFoundException.class)
    public ResponseEntity<String> handleMealNotFound(MealNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(RecipeNotFoundException.class)
    public ResponseEntity<String> handleRecipeNotFound(RecipeNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(MealsShoppingListNotFoundException.class)
    public ResponseEntity<String> handleShoppingListNotFound(MealsShoppingListNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(MealsShoppingDuplicateItemException.class)
    public ResponseEntity<String> handleDuplicateShoppingItem(MealsShoppingDuplicateItemException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
