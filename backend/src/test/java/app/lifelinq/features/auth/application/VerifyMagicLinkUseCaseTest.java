package app.lifelinq.features.auth.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import app.lifelinq.features.auth.domain.MagicLinkChallenge;
import app.lifelinq.features.auth.domain.MagicLinkChallengeRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

class VerifyMagicLinkUseCaseTest {
    private static final String VALID_TOKEN = "AbCdEf0123456789AbCdEf0123456789AbCdEf0123456789";

    @Test
    void consumesValidChallengeAndReturnsNormalizedEmail() {
        InMemoryChallengeRepository repository = new InMemoryChallengeRepository();
        Instant now = Instant.parse("2026-03-03T10:00:00Z");
        repository.save(new MagicLinkChallenge(
                UUID.randomUUID(),
                VALID_TOKEN,
                "User@Example.com",
                now.plusSeconds(600),
                null
        ));

        VerifyMagicLinkUseCase useCase = new VerifyMagicLinkUseCase(repository);
        VerifiedMagicLinkResult result = useCase.execute(new VerifyMagicLinkCommand(VALID_TOKEN, now));

        assertEquals("user@example.com", result.getNormalizedEmail());
        MagicLinkChallenge saved = repository.findByToken(VALID_TOKEN).orElseThrow();
        assertEquals(now, saved.getConsumedAt());
    }

    @Test
    void rejectsExpiredChallenge() {
        InMemoryChallengeRepository repository = new InMemoryChallengeRepository();
        Instant now = Instant.parse("2026-03-03T10:00:00Z");
        repository.save(new MagicLinkChallenge(
                UUID.randomUUID(),
                VALID_TOKEN,
                "user@example.com",
                now.minusSeconds(1),
                null
        ));

        VerifyMagicLinkUseCase useCase = new VerifyMagicLinkUseCase(repository);
        assertThrows(MagicLinkVerificationException.class,
                () -> useCase.execute(new VerifyMagicLinkCommand(VALID_TOKEN, now)));
    }

    @Test
    void rejectsInvalidTokenFormatBeforeLookup() {
        VerifyMagicLinkUseCase useCase = new VerifyMagicLinkUseCase(new InMemoryChallengeRepository());

        assertThrows(MagicLinkVerificationException.class,
                () -> useCase.execute(new VerifyMagicLinkCommand("short-token", Instant.parse("2026-03-03T10:00:00Z"))));
    }

    @Test
    void allowsOnlyOneSuccessfulVerificationDuringConcurrentRequests() throws Exception {
        Instant now = Instant.parse("2026-03-03T10:00:00Z");
        ConcurrentChallengeRepositoryTest repository = new ConcurrentChallengeRepositoryTest(
                new MagicLinkChallenge(UUID.randomUUID(), VALID_TOKEN, "user@example.com", now.plusSeconds(600), null)
        );
        VerifyMagicLinkUseCase useCase = new VerifyMagicLinkUseCase(repository);
        CountDownLatch startGate = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Callable<Boolean> task = () -> {
            try {
                startGate.await(5, TimeUnit.SECONDS);
                useCase.execute(new VerifyMagicLinkCommand(VALID_TOKEN, now));
                return true;
            } catch (MagicLinkVerificationException ex) {
                return false;
            }
        };

        Future<Boolean> first = executor.submit(task);
        Future<Boolean> second = executor.submit(task);
        startGate.countDown();

        boolean firstSuccess = first.get(5, TimeUnit.SECONDS);
        boolean secondSuccess = second.get(5, TimeUnit.SECONDS);
        executor.shutdownNow();

        int successCount = (firstSuccess ? 1 : 0) + (secondSuccess ? 1 : 0);
        assertEquals(1, successCount);
        assertEquals(1, repository.saveAttempts.get());
        MagicLinkChallenge stored = repository.findByToken(VALID_TOKEN).orElseThrow();
        assertNotNull(stored.getConsumedAt());
        assertTrue(stored.getVersion() != null && stored.getVersion() >= 1);
    }

    @Test
    void mapsOptimisticLockFailureToMagicLinkVerificationException() {
        Instant now = Instant.parse("2026-03-03T10:00:00Z");
        MagicLinkChallengeRepository repository = mock(MagicLinkChallengeRepository.class);
        when(repository.findByToken(VALID_TOKEN)).thenReturn(Optional.of(new MagicLinkChallenge(
                UUID.randomUUID(),
                VALID_TOKEN,
                "user@example.com",
                now.plusSeconds(600),
                null,
                0L
        )));
        when(repository.existsByToken(Mockito.anyString())).thenReturn(true);
        doThrow(new ObjectOptimisticLockingFailureException(MagicLinkChallenge.class, UUID.randomUUID()))
                .when(repository).save(any(MagicLinkChallenge.class));

        VerifyMagicLinkUseCase useCase = new VerifyMagicLinkUseCase(repository);
        assertThrows(MagicLinkVerificationException.class,
                () -> useCase.execute(new VerifyMagicLinkCommand(VALID_TOKEN, now)));
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

    private static final class ConcurrentChallengeRepositoryTest implements MagicLinkChallengeRepository {
        private final Map<String, MagicLinkChallenge> byToken = new ConcurrentHashMap<>();
        private final AtomicInteger saveAttempts = new AtomicInteger(0);

        private ConcurrentChallengeRepositoryTest(MagicLinkChallenge challenge) {
            byToken.put(challenge.getToken(), withVersion(challenge, 0L));
        }

        @Override
        public Optional<MagicLinkChallenge> findByToken(String token) {
            MagicLinkChallenge current = byToken.get(token);
            if (current == null) {
                return Optional.empty();
            }
            return Optional.of(withVersion(current, current.getVersion()));
        }

        @Override
        public boolean existsByToken(String token) {
            return byToken.containsKey(token);
        }

        @Override
        public void save(MagicLinkChallenge challenge) {
            byToken.compute(challenge.getToken(), (token, current) -> {
                if (current == null) {
                    return challenge;
                }
                if (current.getVersion() == null || challenge.getVersion() == null
                        || !current.getVersion().equals(challenge.getVersion())) {
                    throw new ObjectOptimisticLockingFailureException(MagicLinkChallenge.class, challenge.getId());
                }
                saveAttempts.incrementAndGet();
                return withVersion(challenge, challenge.getVersion() + 1);
            });
        }

        private MagicLinkChallenge withVersion(MagicLinkChallenge challenge, Long version) {
            return new MagicLinkChallenge(
                    challenge.getId(),
                    challenge.getToken(),
                    challenge.getEmail(),
                    challenge.getExpiresAt(),
                    challenge.getConsumedAt(),
                    version
            );
        }
    }
}
