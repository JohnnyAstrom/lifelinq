package app.lifelinq.features.auth.api;

import app.lifelinq.config.ApiErrorResponse;
import app.lifelinq.features.user.contract.DeleteAccountBlockedException;
import app.lifelinq.features.auth.application.ActiveGroupSelectionConflictException;
import app.lifelinq.features.auth.application.RefreshAuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "app.lifelinq.features.auth.api")
public class AuthExceptionHandler {

    @ExceptionHandler(DeleteAccountBlockedException.class)
    public ResponseEntity<ApiErrorResponse> handleDeleteAccountBlocked(DeleteAccountBlockedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse("DELETE_ACCOUNT_BLOCKED", ex.getMessage()));
    }

    @ExceptionHandler(ActiveGroupSelectionConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleActiveGroupSelectionConflict(ActiveGroupSelectionConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse("ACTIVE_GROUP_CONFLICT", ex.getMessage()));
    }

    @ExceptionHandler(RefreshAuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleRefreshAuthenticationFailure(RefreshAuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorResponse("UNAUTHORIZED", "Unauthorized"));
    }
}
