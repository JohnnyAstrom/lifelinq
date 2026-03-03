package app.lifelinq.features.auth.infrastructure;

import app.lifelinq.features.auth.domain.RefreshTokenHasher;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class HmacSha256RefreshTokenHasher implements RefreshTokenHasher {
    private final byte[] secret;

    public HmacSha256RefreshTokenHasher(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("refresh token hash secret must not be blank");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String hash(String plaintextToken) {
        if (plaintextToken == null || plaintextToken.isBlank()) {
            throw new IllegalArgumentException("plaintextToken must not be blank");
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hmacSha256(plaintextToken));
    }

    @Override
    public boolean matches(String plaintextToken, String hashedToken) {
        if (plaintextToken == null || plaintextToken.isBlank()) {
            return false;
        }
        if (hashedToken == null || hashedToken.isBlank()) {
            return false;
        }
        byte[] expected = hashedToken.getBytes(StandardCharsets.UTF_8);
        byte[] actual = hash(plaintextToken).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(actual, expected);
    }

    private byte[] hmacSha256(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to hash refresh token", ex);
        }
    }
}

