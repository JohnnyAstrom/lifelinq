package app.lifelinq.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

class JwtVerifierTest {
    private static final String SECRET = "test-secret";

    @Test
    void verifiesValidToken() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = createToken(userId, Instant.now().plusSeconds(60));

        JwtClaims claims = new JwtVerifier(SECRET).verify(token);

        assertNull(claims.getHouseholdId());
        assertEquals(userId, claims.getUserId());
    }

    @Test
    void rejectsExpiredToken() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = createToken(userId, Instant.now().minusSeconds(60));

        assertThrows(JwtValidationException.class, () -> new JwtVerifier(SECRET).verify(token));
    }

    @Test
    void rejectsMissingClaims() throws Exception {
        String payloadJson = String.format(
                "{\"exp\":%d}",
                Instant.now().plusSeconds(60).getEpochSecond()
        );
        String token = createToken(payloadJson, SECRET);

        assertThrows(JwtValidationException.class, () -> new JwtVerifier(SECRET).verify(token));
    }

    @Test
    void rejectsInvalidSignature() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = createTokenWithSecret("other-secret", userId, Instant.now().plusSeconds(60));

        assertThrows(JwtValidationException.class, () -> new JwtVerifier(SECRET).verify(token));
    }

    private String createToken(UUID userId, Instant exp) throws Exception {
        String payloadJson = String.format(
                "{\"userId\":\"%s\",\"exp\":%d}",
                userId,
                exp.getEpochSecond()
        );
        return createToken(payloadJson, SECRET);
    }

    private String createToken(String payloadJson, String secret) throws Exception {
        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String headerPart = base64Url(headerJson.getBytes(StandardCharsets.UTF_8));
        String payloadPart = base64Url(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signaturePart = base64Url(hmacSha256(secret, headerPart + "." + payloadPart));
        return headerPart + "." + payloadPart + "." + signaturePart;
    }

    private String createTokenWithSecret(String secret, UUID userId, Instant exp) throws Exception {
        String payloadJson = String.format(
                "{\"userId\":\"%s\",\"exp\":%d}",
                userId,
                exp.getEpochSecond()
        );
        return createToken(payloadJson, secret);
    }

    private byte[] hmacSha256(String secret, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private String base64Url(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }
}
