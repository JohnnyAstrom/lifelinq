package app.lifelinq.features.auth.api;

import app.lifelinq.features.auth.application.AuthApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthLogoutController {
    private final AuthApplicationService authApplicationService;

    public AuthLogoutController(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(@RequestBody(required = false) LogoutRequest request) {
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            return ResponseEntity.badRequest().body("refreshToken must not be blank");
        }
        authApplicationService.logoutRefreshSession(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    public static final class LogoutRequest {
        private String refreshToken;

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}

