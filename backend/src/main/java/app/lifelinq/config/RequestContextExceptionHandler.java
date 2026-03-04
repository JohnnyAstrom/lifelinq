package app.lifelinq.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "app.lifelinq.features")
public final class RequestContextExceptionHandler {

    @ExceptionHandler(NoActiveGroupSelectedException.class)
    public ResponseEntity<ApiErrorResponse> handleNoActiveGroupSelected(NoActiveGroupSelectedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse("NO_ACTIVE_GROUP", ex.getMessage()));
    }
}
