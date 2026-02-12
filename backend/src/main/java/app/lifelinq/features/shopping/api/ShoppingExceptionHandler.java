package app.lifelinq.features.shopping.api;

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
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
    }

    @ExceptionHandler(ShoppingListNotFoundException.class)
    public ResponseEntity<String> handleListNotFound(ShoppingListNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ShoppingItemNotFoundException.class)
    public ResponseEntity<String> handleItemNotFound(ShoppingItemNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(DuplicateShoppingItemNameException.class)
    public ResponseEntity<String> handleDuplicate(DuplicateShoppingItemNameException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
