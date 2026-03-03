package app.lifelinq.features.auth.api;

import app.lifelinq.features.auth.application.AuthApplicationService;
import app.lifelinq.features.auth.application.AuthTokenPair;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthRefreshController {
    private final AuthApplicationService authApplicationService;

    public AuthRefreshController(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            return ResponseEntity.badRequest().body("refreshToken must not be blank");
        }
        AuthTokenPair tokens = authApplicationService.refreshAuthTokens(request.getRefreshToken());
        return ResponseEntity.ok(new RefreshResponse(tokens.accessToken(), tokens.refreshToken()));
    }

    public static final class RefreshRequest {
        private String refreshToken;

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }

    public record RefreshResponse(String accessToken, String refreshToken) {}
}

