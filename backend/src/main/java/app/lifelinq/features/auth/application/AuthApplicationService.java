package app.lifelinq.features.auth.application;

import app.lifelinq.config.JwtSigner;
import app.lifelinq.features.user.application.EnsureUserExistsUseCase;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class AuthApplicationService {
    private final EnsureUserExistsUseCase ensureUserExistsUseCase;
    private final JwtSigner jwtSigner;

    public AuthApplicationService(
            EnsureUserExistsUseCase ensureUserExistsUseCase,
            JwtSigner jwtSigner
    ) {
        if (ensureUserExistsUseCase == null) {
            throw new IllegalArgumentException("ensureUserExistsUseCase must not be null");
        }
        if (jwtSigner == null) {
            throw new IllegalArgumentException("jwtSigner must not be null");
        }
        this.ensureUserExistsUseCase = ensureUserExistsUseCase;
        this.jwtSigner = jwtSigner;
    }

    public String devLogin(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
        UUID userId = UUID.nameUUIDFromBytes(email.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
        ensureUserExistsUseCase.execute(userId);
        return jwtSigner.sign(userId);
    }
}
