package app.lifelinq.config;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class JwtSigner {
    private final byte[] secret;
    private final long ttlSeconds;
    private final String issuer;
    private final String audience;
    private final Clock clock;

    public JwtSigner(String secret, long ttlSeconds, String issuer, String audience, Clock clock) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("JWT secret must not be blank");
        }
        if (ttlSeconds <= 0) {
            throw new IllegalArgumentException("ttlSeconds must be positive");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.ttlSeconds = ttlSeconds;
        this.issuer = issuer;
        this.audience = audience;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    public String sign(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        long exp = Instant.now(clock).plusSeconds(ttlSeconds).getEpochSecond();
        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payloadJson = buildPayload(userId, exp);
        String headerPart = base64Url(headerJson.getBytes(StandardCharsets.UTF_8));
        String payloadPart = base64Url(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signaturePart = base64Url(hmacSha256(headerPart + "." + payloadPart));
        return headerPart + "." + payloadPart + "." + signaturePart;
    }

    private String buildPayload(UUID userId, long exp) {
        StringBuilder json = new StringBuilder();
        json.append("{\"userId\":\"").append(userId).append("\",\"exp\":").append(exp);
        if (issuer != null && !issuer.isBlank()) {
            json.append(",\"iss\":\"").append(issuer).append("\"");
        }
        if (audience != null && !audience.isBlank()) {
            json.append(",\"aud\":\"").append(audience).append("\"");
        }
        json.append("}");
        return json.toString();
    }

    private byte[] hmacSha256(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to sign JWT", ex);
        }
    }

    private String base64Url(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }
}
