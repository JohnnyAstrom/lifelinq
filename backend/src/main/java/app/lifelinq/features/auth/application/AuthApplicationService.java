package app.lifelinq.features.auth.application;

import app.lifelinq.config.JwtSigner;
import app.lifelinq.features.user.contract.UserProvisioning;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class AuthApplicationService {
    private final UserProvisioning userProvisioning;
    private final JwtSigner jwtSigner;

    public AuthApplicationService(
            UserProvisioning userProvisioning,
            JwtSigner jwtSigner
    ) {
        if (userProvisioning == null) {
            throw new IllegalArgumentException("userProvisioning must not be null");
        }
        if (jwtSigner == null) {
            throw new IllegalArgumentException("jwtSigner must not be null");
        }
        this.userProvisioning = userProvisioning;
        this.jwtSigner = jwtSigner;
    }

    public String devLogin(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
        UUID userId = UUID.nameUUIDFromBytes(email.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
        userProvisioning.ensureUserExists(userId);
        return jwtSigner.sign(userId);
    }
}
