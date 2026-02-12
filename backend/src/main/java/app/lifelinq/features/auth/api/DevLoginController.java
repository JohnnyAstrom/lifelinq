package app.lifelinq.features.auth.api;

import app.lifelinq.config.JwtSigner;
import app.lifelinq.features.user.application.EnsureUserExistsUseCase;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("dev")
public class DevLoginController {
    private final EnsureUserExistsUseCase ensureUserExistsUseCase;
    private final JwtSigner jwtSigner;

    public DevLoginController(EnsureUserExistsUseCase ensureUserExistsUseCase, JwtSigner jwtSigner) {
        this.ensureUserExistsUseCase = ensureUserExistsUseCase;
        this.jwtSigner = jwtSigner;
    }

    // Development-only endpoint. Do not expose in production.
    @PostMapping("/auth/dev-login")
    public ResponseEntity<DevLoginResponse> devLogin(@RequestBody DevLoginRequest request) {
        if (request == null || request.email() == null || request.email().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        UUID userId = UUID.nameUUIDFromBytes(request.email().trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
        ensureUserExistsUseCase.execute(userId);
        String token = jwtSigner.sign(userId);
        return ResponseEntity.ok(new DevLoginResponse(token));
    }

    public record DevLoginRequest(String email) {}

    public record DevLoginResponse(String token) {}
}
