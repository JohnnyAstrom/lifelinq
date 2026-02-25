package app.lifelinq.features.group.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.lifelinq.features.group.domain.Invitation;
import app.lifelinq.features.group.domain.InvitationRepository;
import app.lifelinq.features.group.domain.InvitationStatus;
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
        CreateInvitationUseCase useCase = new CreateInvitationUseCase(repository, tokenGenerator);

        CreateInvitationCommand command = new CreateInvitationCommand(
                UUID.randomUUID(),
                "test@example.com",
                Instant.parse("2026-01-01T00:00:00Z"),
                Duration.ofDays(2)
        );

        CreateInvitationResult result = useCase.execute(command);

        assertNotNull(result.getInvitationId());
        assertEquals("token-1", result.getToken());
        assertEquals(Instant.parse("2026-01-03T00:00:00Z"), result.getExpiresAt());
        assertEquals(1, repository.saved.size());
        assertEquals(InvitationStatus.ACTIVE, repository.saved.get(0).getStatus());
    }

    @Test
    void requiresCommand() {
        CreateInvitationUseCase useCase = new CreateInvitationUseCase(new InMemoryInvitationRepository(), new FixedTokenGenerator("t"));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    @Test
    void requiresPositiveTtl() {
        CreateInvitationUseCase useCase = new CreateInvitationUseCase(new InMemoryInvitationRepository(), new FixedTokenGenerator("t"));
        CreateInvitationCommand command = new CreateInvitationCommand(
                UUID.randomUUID(),
                "test@example.com",
                Instant.parse("2026-01-01T00:00:00Z"),
                Duration.ZERO
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
                new SequenceTokenGenerator(List.of("token-1", "token-2"))
        );

        CreateInvitationCommand command = new CreateInvitationCommand(
                UUID.randomUUID(),
                "test@example.com",
                Instant.parse("2026-01-01T00:00:00Z"),
                Duration.ofDays(1)
        );

        CreateInvitationResult result = useCase.execute(command);

        assertEquals("token-2", result.getToken());
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
        public boolean existsByToken(String token) {
            return findByToken(token).isPresent();
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
}
