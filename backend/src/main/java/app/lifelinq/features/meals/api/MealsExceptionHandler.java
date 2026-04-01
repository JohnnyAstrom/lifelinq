package app.lifelinq.features.meals.api;

import app.lifelinq.config.ApiErrorResponse;
import app.lifelinq.features.meals.application.MealsAccessDeniedException;
import app.lifelinq.features.meals.application.MealNotFoundException;
import app.lifelinq.features.meals.application.RecipeDeleteBlockedException;
import app.lifelinq.features.meals.application.RecipeDraftNotFoundException;
import app.lifelinq.features.meals.application.RecipeDuplicateAttentionRequiredException;
import app.lifelinq.features.meals.application.RecipeImportFailedException;
import app.lifelinq.features.meals.contract.MealsShoppingAccessDeniedException;
import app.lifelinq.features.meals.contract.MealsShoppingDuplicateItemException;
import app.lifelinq.features.meals.contract.MealsShoppingListNotFoundException;
import app.lifelinq.features.meals.application.RecipeNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "app.lifelinq.features.meals.api")
public final class MealsExceptionHandler {

    @ExceptionHandler(MealsAccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(MealsAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiErrorResponse("ACCESS_DENIED", "Access denied"));
    }

    @ExceptionHandler(MealsShoppingAccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleShoppingAccessDenied(
            MealsShoppingAccessDeniedException ex
    ) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiErrorResponse("ACCESS_DENIED", "Access denied"));
    }

    @ExceptionHandler(MealNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleMealNotFound(MealNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse("MEAL_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(RecipeNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleRecipeNotFound(RecipeNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse("RECIPE_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(RecipeDraftNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleRecipeDraftNotFound(RecipeDraftNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse("RECIPE_DRAFT_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(MealsShoppingListNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleShoppingListNotFound(MealsShoppingListNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse("SHOPPING_LIST_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(MealsShoppingDuplicateItemException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateShoppingItem(MealsShoppingDuplicateItemException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse("DUPLICATE_ITEM", ex.getMessage()));
    }

    @ExceptionHandler(RecipeImportFailedException.class)
    public ResponseEntity<ApiErrorResponse> handleRecipeImportFailed(RecipeImportFailedException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiErrorResponse("RECIPE_IMPORT_FAILED", ex.getMessage()));
    }

    @ExceptionHandler(RecipeDeleteBlockedException.class)
    public ResponseEntity<ApiErrorResponse> handleRecipeDeleteBlocked(RecipeDeleteBlockedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse("RECIPE_DELETE_BLOCKED", ex.getMessage()));
    }

    @ExceptionHandler(RecipeDuplicateAttentionRequiredException.class)
    public ResponseEntity<ApiErrorResponse> handleRecipeDuplicateAttentionRequired(
            RecipeDuplicateAttentionRequiredException ex
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse("RECIPE_DUPLICATE_ATTENTION_REQUIRED", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse("BAD_REQUEST", ex.getMessage()));
    }
}
