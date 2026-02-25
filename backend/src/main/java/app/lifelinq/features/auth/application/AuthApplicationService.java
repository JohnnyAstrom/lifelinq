package app.lifelinq.features.auth.application;

import app.lifelinq.config.JwtSigner;
import app.lifelinq.features.user.contract.UserAccountDeletion;
import app.lifelinq.features.user.contract.UserProvisioning;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class AuthApplicationService {
    private final UserProvisioning userProvisioning;
    private final UserAccountDeletion userAccountDeletion;
    private final JwtSigner jwtSigner;

    public AuthApplicationService(
            UserProvisioning userProvisioning,
            UserAccountDeletion userAccountDeletion,
            JwtSigner jwtSigner
    ) {
        if (userProvisioning == null) {
            throw new IllegalArgumentException("userProvisioning must not be null");
        }
        if (userAccountDeletion == null) {
            throw new IllegalArgumentException("userAccountDeletion must not be null");
        }
        if (jwtSigner == null) {
            throw new IllegalArgumentException("jwtSigner must not be null");
        }
        this.userProvisioning = userProvisioning;
        this.userAccountDeletion = userAccountDeletion;
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

    public void deleteAccount(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        userAccountDeletion.deleteAccount(userId);
    }
}
