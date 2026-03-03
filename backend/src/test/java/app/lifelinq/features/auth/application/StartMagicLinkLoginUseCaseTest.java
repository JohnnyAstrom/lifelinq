package app.lifelinq.features.auth.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.auth.domain.AuthMailSender;
import app.lifelinq.features.auth.domain.MagicLinkChallenge;
import app.lifelinq.features.auth.domain.MagicLinkChallengeRepository;
import app.lifelinq.features.auth.domain.MagicLinkTokenGenerator;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class StartMagicLinkLoginUseCaseTest {

    @Test
    void createsChallengeAndSendsVerificationLink() {
        InMemoryChallengeRepository repository = new InMemoryChallengeRepository();
        CapturingMailSender mailSender = new CapturingMailSender();
        StartMagicLinkLoginUseCase useCase = new StartMagicLinkLoginUseCase(
                repository,
                () -> "TOKEN123",
                mailSender
        );
        Instant now = Instant.parse("2026-03-03T10:00:00Z");

        useCase.execute(new StartMagicLinkLoginCommand(
                "  User@Example.com ",
                now,
                Duration.ofMinutes(15),
                "http://localhost:8080/auth/magic/verify"
        ));

        MagicLinkChallenge challenge = repository.findByToken("TOKEN123").orElseThrow();
        assertEquals("user@example.com", challenge.getEmail());
        assertEquals(now.plus(Duration.ofMinutes(15)), challenge.getExpiresAt());
        assertNotNull(challenge.getId());
        assertEquals("user@example.com", mailSender.email);
        assertTrue(mailSender.verifyUrl.contains("token=TOKEN123"));
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

    private static final class CapturingMailSender implements AuthMailSender {
        private String email;
        private String verifyUrl;

        @Override
        public void sendMagicLink(String email, String verifyUrl) {
            this.email = email;
            this.verifyUrl = verifyUrl;
        }
    }
}

