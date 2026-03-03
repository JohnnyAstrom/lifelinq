package app.lifelinq.features.auth.infrastructure;

import app.lifelinq.features.auth.domain.RefreshTokenGenerator;
import java.security.SecureRandom;
import java.util.Base64;

public final class SecureRefreshTokenGenerator implements RefreshTokenGenerator {
    private static final int BYTE_LENGTH = 48;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate() {
        byte[] bytes = new byte[BYTE_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

