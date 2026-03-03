package app.lifelinq.features.auth.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.auth.domain.RefreshSession;
import app.lifelinq.features.auth.domain.RefreshSessionRepository;
import app.lifelinq.features.auth.domain.RefreshToken;
import app.lifelinq.features.auth.domain.RefreshTokenGenerator;
import app.lifelinq.features.auth.domain.RefreshTokenHasher;
import app.lifelinq.features.auth.domain.RefreshTokenRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RotateRefreshTokenUseCaseTest {

    @Test
    void rotatesTokenAndMarksCurrentTokenAsUsed() {
        Instant now = Instant.parse("2026-03-03T10:00:00Z");
        InMemoryRefreshSessionRepository sessions = new InMemoryRefreshSessionRepository();
        InMemoryRefreshTokenRepository tokens = new InMemoryRefreshTokenRepository();
        PrefixHasher hasher = new PrefixHasher();
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        sessions.save(new RefreshSession(sessionId, userId, now.minusSeconds(60), now.plus(Duration.ofDays(90)), null, null));
        tokens.save(new RefreshToken(
                UUID.randomUUID(),
                sessionId,
                hasher.hash("old-refresh"),
                now.minusSeconds(60),
                now.plus(Duration.ofDays(30)),
                null,
                null,
                null
        ));

        RotateRefreshTokenUseCase useCase = new RotateRefreshTokenUseCase(
                sessions,
                tokens,
                new StaticSequenceTokenGenerator("new-refresh"),
                hasher
        );

        RotateRefreshTokenResult result = useCase.execute("old-refresh", now, Duration.ofDays(30));

        assertEquals(userId, result.userId());
        assertEquals("new-refresh", result.refreshToken());

        RefreshToken oldToken = tokens.findByTokenHash(hasher.hash("old-refresh")).orElseThrow();
        assertNotNull(oldToken.getUsedAt());
        assertNotNull(oldToken.getReplacedByTokenId());

        RefreshToken newToken = tokens.findByTokenHash(hasher.hash("new-refresh")).orElseThrow();
        assertEquals(oldToken.getReplacedByTokenId(), newToken.getId());
        assertTrue(newToken.getUsedAt() == null);
    }

    @Test
    void secondUseDuringRotationRevokesSessionAsReplay() {
        Instant now = Instant.parse("2026-03-03T10:00:00Z");
        InMemoryRefreshSessionRepository sessions = new InMemoryRefreshSessionRepository();
        InMemoryRefreshTokenRepository tokens = new InMemoryRefreshTokenRepository();
        PrefixHasher hasher = new PrefixHasher();
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        sessions.save(new RefreshSession(sessionId, userId, now.minusSeconds(60), now.plus(Duration.ofDays(90)), null, null));
        RefreshToken current = new RefreshToken(
                UUID.randomUUID(),
                sessionId,
                hasher.hash("old-refresh"),
                now.minusSeconds(60),
                now.plus(Duration.ofDays(30)),
                null,
                null,
                null
        );
        tokens.save(current);

        RotateRefreshTokenUseCase useCase = new RotateRefreshTokenUseCase(
                sessions,
                tokens,
                new StaticSequenceTokenGenerator("new-refresh"),
                hasher
        );

        useCase.execute("old-refresh", now, Duration.ofDays(30));
        assertThrows(RefreshAuthenticationException.class,
                () -> useCase.execute("old-refresh", now.plusSeconds(1), Duration.ofDays(30)));

        RefreshSession session = sessions.findById(sessionId).orElseThrow();
        assertTrue(session.isRevoked());
        assertEquals("REPLAY", session.getRevokeReason());
    }

    private static final class PrefixHasher implements RefreshTokenHasher {
        @Override
        public String hash(String plaintextToken) {
            return "hash:" + plaintextToken;
        }

        @Override
        public boolean matches(String plaintextToken, String hashedToken) {
            return ("hash:" + plaintextToken).equals(hashedToken);
        }
    }

    private static final class StaticSequenceTokenGenerator implements RefreshTokenGenerator {
        private final String value;

        private StaticSequenceTokenGenerator(String value) {
            this.value = value;
        }

        @Override
        public String generate() {
            return value;
        }
    }

    private static final class InMemoryRefreshSessionRepository implements RefreshSessionRepository {
        private final Map<UUID, RefreshSession> byId = new HashMap<>();

        @Override
        public void save(RefreshSession refreshSession) {
            byId.put(refreshSession.getId(), refreshSession);
        }

        @Override
        public Optional<RefreshSession> findById(UUID id) {
            return Optional.ofNullable(byId.get(id));
        }
    }

    private static final class InMemoryRefreshTokenRepository implements RefreshTokenRepository {
        private final Map<UUID, RefreshToken> byId = new HashMap<>();
        private final Map<String, UUID> idByHash = new HashMap<>();

        @Override
        public void save(RefreshToken refreshToken) {
            byId.put(refreshToken.getId(), refreshToken);
            idByHash.put(refreshToken.getTokenHash(), refreshToken.getId());
        }

        @Override
        public Optional<RefreshToken> findById(UUID id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<RefreshToken> findByTokenHash(String tokenHash) {
            UUID id = idByHash.get(tokenHash);
            if (id == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(byId.get(id));
        }

    }
}
