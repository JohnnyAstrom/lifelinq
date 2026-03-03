package app.lifelinq.features.auth.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.lifelinq.features.auth.domain.MagicLinkChallenge;
import app.lifelinq.features.auth.domain.MagicLinkChallengeRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class VerifyMagicLinkUseCaseTest {

    @Test
    void consumesValidChallengeAndReturnsNormalizedEmail() {
        InMemoryChallengeRepository repository = new InMemoryChallengeRepository();
        Instant now = Instant.parse("2026-03-03T10:00:00Z");
        repository.save(new MagicLinkChallenge(
                UUID.randomUUID(),
                "abc-token",
                "User@Example.com",
                now.plusSeconds(600),
                null
        ));

        VerifyMagicLinkUseCase useCase = new VerifyMagicLinkUseCase(repository);
        VerifiedMagicLinkResult result = useCase.execute(new VerifyMagicLinkCommand("abc-token", now));

        assertEquals("user@example.com", result.getNormalizedEmail());
        MagicLinkChallenge saved = repository.findByToken("abc-token").orElseThrow();
        assertEquals(now, saved.getConsumedAt());
    }

    @Test
    void rejectsExpiredChallenge() {
        InMemoryChallengeRepository repository = new InMemoryChallengeRepository();
        Instant now = Instant.parse("2026-03-03T10:00:00Z");
        repository.save(new MagicLinkChallenge(
                UUID.randomUUID(),
                "expired-token",
                "user@example.com",
                now.minusSeconds(1),
                null
        ));

        VerifyMagicLinkUseCase useCase = new VerifyMagicLinkUseCase(repository);
        assertThrows(MagicLinkVerificationException.class,
                () -> useCase.execute(new VerifyMagicLinkCommand("expired-token", now)));
    }

    private static final class InMemoryChallengeRepository implements MagicLinkChallengeRepository {
        private final Map<String, MagicLinkChallenge> byToken = new HashMap<>();

        @Override
        public Optional<MagicLinkChallenge> findByToken(String token) {
            return Optional.ofNullable(byToken.get(token));
        }

        @Override
        public boolean existsByToken(String token) {
            return byToken.containsKey(token);
        }

        @Override
        public void save(MagicLinkChallenge challenge) {
            byToken.put(challenge.getToken(), challenge);
        }
    }
}

