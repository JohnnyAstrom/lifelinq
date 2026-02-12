package app.lifelinq.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtSignerTest {
    private static final String SECRET = "test-secret";

    @Test
    void signerProducesTokenThatVerifierAccepts() {
        Instant now = Instant.now();
        Clock clock = Clock.fixed(now, ZoneOffset.UTC);
        JwtSigner signer = new JwtSigner(SECRET, 300, "issuer", "audience", clock);
        UUID userId = UUID.randomUUID();

        String token = signer.sign(userId);

        JwtClaims claims = new JwtVerifier(SECRET).verify(token);
        assertEquals(userId, claims.getUserId());
        assertNull(claims.getHouseholdId());
    }
}
