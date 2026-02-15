package app.lifelinq.features.auth.application;

import app.lifelinq.config.JwtSigner;
import app.lifelinq.features.user.application.UserApplicationService;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class AuthApplicationService {
    private final UserApplicationService userApplicationService;
    private final JwtSigner jwtSigner;

    public AuthApplicationService(
            UserApplicationService userApplicationService,
            JwtSigner jwtSigner
    ) {
        if (userApplicationService == null) {
            throw new IllegalArgumentException("userApplicationService must not be null");
        }
        if (jwtSigner == null) {
            throw new IllegalArgumentException("jwtSigner must not be null");
        }
        this.userApplicationService = userApplicationService;
        this.jwtSigner = jwtSigner;
    }

    public String devLogin(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
        UUID userId = UUID.nameUUIDFromBytes(email.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
        userApplicationService.ensureUserExists(userId);
        return jwtSigner.sign(userId);
    }
}
