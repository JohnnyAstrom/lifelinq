package app.lifelinq.features.auth.api;

import app.lifelinq.features.user.contract.DeleteAccountBlockedException;
import app.lifelinq.features.auth.application.ActiveGroupSelectionConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "app.lifelinq.features.auth.api")
public class AuthExceptionHandler {

    @ExceptionHandler(DeleteAccountBlockedException.class)
    public ResponseEntity<String> handleDeleteAccountBlocked(DeleteAccountBlockedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(ActiveGroupSelectionConflictException.class)
    public ResponseEntity<String> handleActiveGroupSelectionConflict(ActiveGroupSelectionConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
