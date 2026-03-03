package app.lifelinq.features.group.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.lifelinq.features.group.domain.Invitation;
import app.lifelinq.features.group.domain.InvitationRepository;
import app.lifelinq.features.group.domain.InvitationStatus;
import app.lifelinq.features.group.domain.InvitationType;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CreateInvitationUseCaseTest {

    @Test
    void createsInvitationWithTokenAndExpiry() {
        InMemoryInvitationRepository repository = new InMemoryInvitationRepository();
        FixedTokenGenerator tokenGenerator = new FixedTokenGenerator("token-1");
        FixedShortCodeGenerator shortCodeGenerator = new FixedShortCodeGenerator("AB12CD");
        CreateInvitationUseCase useCase = new CreateInvitationUseCase(repository, tokenGenerator, shortCodeGenerator);

        CreateInvitationCommand command = new CreateInvitationCommand(
                UUID.randomUUID(),
                InvitationType.EMAIL,
                "test@example.com",
                "Alex Doe",
                Instant.parse("2026-01-01T00:00:00Z"),
                Duration.ofDays(2),
                1
        );

        CreateInvitationResult result = useCase.execute(command);

        assertNotNull(result.getInvitationId());
        assertEquals("token-1", result.getToken());
        assertEquals(Instant.parse("2026-01-03T00:00:00Z"), result.getExpiresAt());
        assertEquals(1, repository.saved.size());
        assertEquals(InvitationStatus.ACTIVE, repository.saved.get(0).getStatus());
        assertEquals("Alex Doe", repository.saved.get(0).getInviterDisplayName());
        assertEquals("AB12CD", repository.saved.get(0).getShortCode());
    }

    @Test
    void requiresCommand() {
        CreateInvitationUseCase useCase = new CreateInvitationUseCase(
                new InMemoryInvitationRepository(),
                new FixedTokenGenerator("token-1"),
                new FixedShortCodeGenerator("AB12CD")
        );
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    @Test
    void requiresPositiveTtl() {
        CreateInvitationUseCase useCase = new CreateInvitationUseCase(
                new InMemoryInvitationRepository(),
                new FixedTokenGenerator("token-1"),
                new FixedShortCodeGenerator("AB12CD")
        );
        CreateInvitationCommand command = new CreateInvitationCommand(
                UUID.randomUUID(),
                InvitationType.EMAIL,
                "test@example.com",
                Instant.parse("2026-01-01T00:00:00Z"),
                Duration.ZERO,
                1
        );
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }

    @Test
    void generatesNewTokenWhenCollision() {
        InMemoryInvitationRepository repository = new InMemoryInvitationRepository();
        repository.saved.add(Invitation.rehydrate(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "test@example.com",
                "token-1",
                Instant.parse("2026-01-02T00:00:00Z"),
                InvitationStatus.ACTIVE
        ));

        CreateInvitationUseCase useCase = new CreateInvitationUseCase(
                repository,
                new SequenceTokenGenerator(List.of("token-1", "token-2")),
                new FixedShortCodeGenerator("AB12CD")
        );

        CreateInvitationCommand command = new CreateInvitationCommand(
                UUID.randomUUID(),
                InvitationType.EMAIL,
                "test@example.com",
                Instant.parse("2026-01-01T00:00:00Z"),
                Duration.ofDays(1),
                1
        );

        CreateInvitationResult result = useCase.execute(command);

        assertEquals("token-2", result.getToken());
    }

    @Test
    void createsLinkInvitationWithoutEmail() {
        InMemoryInvitationRepository repository = new InMemoryInvitationRepository();
        FixedTokenGenerator tokenGenerator = new FixedTokenGenerator("link-token");
        FixedShortCodeGenerator shortCodeGenerator = new FixedShortCodeGenerator("ZX90QP");
        CreateInvitationUseCase useCase = new CreateInvitationUseCase(repository, tokenGenerator, shortCodeGenerator);

        CreateInvitationCommand command = new CreateInvitationCommand(
                UUID.randomUUID(),
                InvitationType.LINK,
                null,
                Instant.parse("2026-01-01T00:00:00Z"),
                Duration.ofDays(2),
                null
        );

        CreateInvitationResult result = useCase.execute(command);

        assertNotNull(result.getInvitationId());
        assertEquals("link-token", result.getToken());
        assertEquals(1, repository.saved.size());
        assertEquals(InvitationType.LINK, repository.saved.get(0).getType());
        assertEquals(null, repository.saved.get(0).getInviteeEmail());
        assertEquals(null, repository.saved.get(0).getMaxUses());
        assertEquals("ZX90QP", repository.saved.get(0).getShortCode());
    }

    @Test
    void rejectsCreateWhenActiveLinkAlreadyExists() {
        InMemoryInvitationRepository repository = new InMemoryInvitationRepository();
        UUID groupId = UUID.randomUUID();
        repository.saved.add(Invitation.createActive(
                UUID.randomUUID(),
                groupId,
                InvitationType.LINK,
                null,
                "existing-link-token",
                Instant.parse("2026-01-03T00:00:00Z"),
                null
        ));
        FixedTokenGenerator tokenGenerator = new FixedTokenGenerator("new-token-ignored");
        CreateInvitationUseCase useCase = new CreateInvitationUseCase(
                repository,
                tokenGenerator,
                new FixedShortCodeGenerator("CD34EF")
        );

        CreateInvitationCommand command = new CreateInvitationCommand(
                groupId,
                InvitationType.LINK,
                null,
                Instant.parse("2026-01-01T00:00:00Z"),
                Duration.ofDays(2),
                null
        );

        assertThrows(IllegalStateException.class, () -> useCase.execute(command));
        assertEquals(1, repository.saved.size());
    }

    @Test
    void retriesShortCodeGenerationOnCollision() {
        InMemoryInvitationRepository repository = new InMemoryInvitationRepository();
        repository.saved.add(Invitation.createActive(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "test@example.com",
                "existing-token",
                "ABC123",
                Instant.parse("2026-01-03T00:00:00Z"),
                1
        ));
        CreateInvitationUseCase useCase = new CreateInvitationUseCase(
                repository,
                new FixedTokenGenerator("token-2"),
                new SequenceShortCodeGenerator(List.of("ABC123", "DEF456"))
        );

        CreateInvitationCommand command = new CreateInvitationCommand(
                UUID.randomUUID(),
                InvitationType.EMAIL,
                "new@example.com",
                Instant.parse("2026-01-01T00:00:00Z"),
                Duration.ofDays(1),
                1
        );

        CreateInvitationResult result = useCase.execute(command);

        assertEquals("token-2", result.getToken());
        assertEquals("DEF456", repository.saved.get(1).getShortCode());
    }

    private static final class InMemoryInvitationRepository implements InvitationRepository {
        private final List<Invitation> saved = new ArrayList<>();

        @Override
        public void save(Invitation invitation) {
            saved.add(invitation);
        }

        @Override
        public Optional<Invitation> findByToken(String token) {
            for (Invitation invitation : saved) {
                if (token.equals(invitation.getToken())) {
                    return Optional.of(invitation);
                }
            }
            return Optional.empty();
        }

        @Override
        public Optional<Invitation> findById(UUID id) {
            for (Invitation invitation : saved) {
                if (id.equals(invitation.getId())) {
                    return Optional.of(invitation);
                }
            }
            return Optional.empty();
        }

        @Override
        public Optional<Invitation> findByShortCode(String shortCode) {
            for (Invitation invitation : saved) {
                if (shortCode.equals(invitation.getShortCode())) {
                    return Optional.of(invitation);
                }
            }
            return Optional.empty();
        }

        @Override
        public boolean existsByToken(String token) {
            return findByToken(token).isPresent();
        }

        @Override
        public boolean existsByShortCode(String shortCode) {
            return findByShortCode(shortCode).isPresent();
        }

        @Override
        public List<Invitation> findActive() {
            List<Invitation> result = new ArrayList<>();
            for (Invitation invitation : saved) {
                if (invitation.getStatus() == InvitationStatus.ACTIVE) {
                    result.add(invitation);
                }
            }
            return result;
        }

        @Override
        public Optional<Invitation> findActiveByGroupIdAndInviteeEmail(UUID groupId, String inviteeEmail) {
            for (Invitation invitation : saved) {
                if (invitation.getStatus() == InvitationStatus.ACTIVE
                        && groupId.equals(invitation.getGroupId())
                        && inviteeEmail.equals(invitation.getInviteeEmail())) {
                    return Optional.of(invitation);
                }
            }
            return Optional.empty();
        }
    }

    private static final class FixedTokenGenerator implements InvitationTokenGenerator {
        private final String token;

        private FixedTokenGenerator(String token) {
            this.token = token;
        }

        @Override
        public String generate() {
            return token;
        }
    }

    private static final class SequenceTokenGenerator implements InvitationTokenGenerator {
        private final List<String> tokens;
        private int index = 0;

        private SequenceTokenGenerator(List<String> tokens) {
            this.tokens = tokens;
        }

        @Override
        public String generate() {
            String token = tokens.get(index);
            index = Math.min(index + 1, tokens.size() - 1);
            return token;
        }
    }

    private static final class FixedShortCodeGenerator implements InvitationShortCodeGenerator {
        private final String shortCode;

        private FixedShortCodeGenerator(String shortCode) {
            this.shortCode = shortCode;
        }

        @Override
        public String generate() {
            return shortCode;
        }
    }

    private static final class SequenceShortCodeGenerator implements InvitationShortCodeGenerator {
        private final List<String> shortCodes;
        private int index = 0;

        private SequenceShortCodeGenerator(List<String> shortCodes) {
            this.shortCodes = shortCodes;
        }

        @Override
        public String generate() {
            String shortCode = shortCodes.get(index);
            index = Math.min(index + 1, shortCodes.size() - 1);
            return shortCode;
        }
    }
}
