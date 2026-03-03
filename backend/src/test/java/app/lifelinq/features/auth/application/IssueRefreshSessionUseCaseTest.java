package app.lifelinq.features.auth.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

class IssueRefreshSessionUseCaseTest {

    @Test
    void issuesRefreshSessionAndStoresHashedRefreshToken() {
        InMemoryRefreshSessionRepository sessions = new InMemoryRefreshSessionRepository();
        InMemoryRefreshTokenRepository tokens = new InMemoryRefreshTokenRepository();
        RefreshTokenGenerator generator = () -> "plain-refresh-token";
        RefreshTokenHasher hasher = new PrefixHasher();

        IssueRefreshSessionUseCase useCase = new IssueRefreshSessionUseCase(sessions, tokens, generator, hasher);
        UUID userId = UUID.randomUUID();
        Instant now = Instant.parse("2026-03-03T10:00:00Z");

        IssueRefreshSessionResult result = useCase.execute(userId, now, Duration.ofDays(30), Duration.ofDays(90));

        assertEquals("plain-refresh-token", result.refreshToken());
        RefreshSession storedSession = sessions.findById(result.sessionId()).orElseThrow();
        assertEquals(userId, storedSession.getUserId());
        assertEquals(now.plus(Duration.ofDays(90)), storedSession.getAbsoluteExpiresAt());

        RefreshToken storedToken = tokens.findBySessionId(result.sessionId()).orElseThrow();
        assertNotNull(storedToken.getTokenHash());
        assertTrue(storedToken.getTokenHash().startsWith("hash:"));
        assertEquals(now.plus(Duration.ofDays(30)), storedToken.getIdleExpiresAt());
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

        Optional<RefreshToken> findBySessionId(UUID sessionId) {
            return byId.values().stream().filter(token -> token.getSessionId().equals(sessionId)).findFirst();
        }
    }
}

