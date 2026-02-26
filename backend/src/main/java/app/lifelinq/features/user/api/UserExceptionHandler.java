package app.lifelinq.features.user.api;

import app.lifelinq.features.user.contract.InvalidUserProfileException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "app.lifelinq.features.user.api")
public final class UserExceptionHandler {

    @ExceptionHandler(InvalidUserProfileException.class)
    public ResponseEntity<String> handleInvalidUserProfile(InvalidUserProfileException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
