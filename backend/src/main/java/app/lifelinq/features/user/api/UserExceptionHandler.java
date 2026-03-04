package app.lifelinq.features.user.api;

import app.lifelinq.config.ApiErrorResponse;
import app.lifelinq.features.user.contract.InvalidUserProfileException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "app.lifelinq.features.user.api")
public final class UserExceptionHandler {

    @ExceptionHandler(InvalidUserProfileException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidUserProfile(InvalidUserProfileException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse("INVALID_USER_PROFILE", ex.getMessage()));
    }
}
