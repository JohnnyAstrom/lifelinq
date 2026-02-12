package app.lifelinq.features.auth.api;

import app.lifelinq.features.auth.application.AuthApplicationService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("dev")
public class DevLoginController {
    private final AuthApplicationService authApplicationService;

    public DevLoginController(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    // Development-only endpoint. Do not expose in production.
    @PostMapping("/auth/dev-login")
    public ResponseEntity<DevLoginResponse> devLogin(@RequestBody DevLoginRequest request) {
        if (request == null || request.email() == null || request.email().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        String token = authApplicationService.devLogin(request.email());
        return ResponseEntity.ok(new DevLoginResponse(token));
    }

    public record DevLoginRequest(String email) {}

    public record DevLoginResponse(String token) {}
}
