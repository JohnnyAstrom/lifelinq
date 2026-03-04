package app.lifelinq.features.shopping.api;

import app.lifelinq.config.ApiErrorResponse;
import app.lifelinq.features.shopping.application.AccessDeniedException;
import app.lifelinq.features.shopping.domain.DuplicateShoppingItemNameException;
import app.lifelinq.features.shopping.domain.ShoppingItemNotFoundException;
import app.lifelinq.features.shopping.domain.ShoppingListNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "app.lifelinq.features.shopping.api")
public final class ShoppingExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiErrorResponse("ACCESS_DENIED", "Access denied"));
    }

    @ExceptionHandler(ShoppingListNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleListNotFound(ShoppingListNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse("SHOPPING_LIST_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(ShoppingItemNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleItemNotFound(ShoppingItemNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse("SHOPPING_ITEM_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateShoppingItemNameException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicate(DuplicateShoppingItemNameException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse("DUPLICATE_ITEM_NAME", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse("BAD_REQUEST", ex.getMessage()));
    }
}
