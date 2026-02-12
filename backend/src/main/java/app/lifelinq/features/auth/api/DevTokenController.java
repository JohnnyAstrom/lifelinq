package app.lifelinq.features.auth.api;

import app.lifelinq.config.JwtSigner;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DevTokenController {
    private final JwtSigner jwtSigner;

    public DevTokenController(JwtSigner jwtSigner) {
        this.jwtSigner = jwtSigner;
    }

    // Development-only endpoint. Do not expose in production.
    @PostMapping("/dev/token")
    public ResponseEntity<DevTokenResponse> createToken(@RequestBody DevTokenRequest request) {
        if (request == null || request.userId() == null || request.userId().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        UUID userId;
        try {
            userId = UUID.fromString(request.userId());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
        String token = jwtSigner.sign(userId);
        return ResponseEntity.ok(new DevTokenResponse(token));
    }

    public record DevTokenRequest(String userId) {}

    public record DevTokenResponse(String token) {}
}
