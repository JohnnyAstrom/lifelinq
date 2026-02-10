package app.lifelinq.features.household.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.lifelinq.features.household.domain.Invitation;
import app.lifelinq.features.household.domain.InvitationRepository;
import app.lifelinq.features.household.domain.InvitationStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RevokeInvitationUseCaseTest {

    @Test
    void revokesWhenPending() {
        InMemoryInvitationRepository repository = new InMemoryInvitationRepository();
        Invitation invitation = new Invitation(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "test@example.com",
                "token-1",
                Instant.parse("2026-01-01T00:00:00Z"),
                InvitationStatus.PENDING
        );
        repository.save(invitation);

        RevokeInvitationUseCase useCase = new RevokeInvitationUseCase(repository);
        RevokeInvitationCommand command = new RevokeInvitationCommand(
                "token-1",
                Instant.parse("2025-12-31T00:00:00Z")
        );

        RevokeInvitationResult result = useCase.execute(command);

        assertEquals(true, result.isRevoked());
        assertEquals(InvitationStatus.REVOKED, repository.saved.get(0).getStatus());
    }

    @Test
    void returnsFalseWhenNotFound() {
        RevokeInvitationUseCase useCase = new RevokeInvitationUseCase(new InMemoryInvitationRepository());
        RevokeInvitationCommand command = new RevokeInvitationCommand(
                "missing",
                Instant.parse("2025-12-31T00:00:00Z")
        );
        RevokeInvitationResult result = useCase.execute(command);
        assertEquals(false, result.isRevoked());
    }

    @Test
    void returnsFalseWhenNotPending() {
        InMemoryInvitationRepository repository = new InMemoryInvitationRepository();
        Invitation invitation = new Invitation(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "test@example.com",
                "token-1",
                Instant.parse("2026-01-01T00:00:00Z"),
                InvitationStatus.ACCEPTED
        );
        repository.save(invitation);

        RevokeInvitationUseCase useCase = new RevokeInvitationUseCase(repository);
        RevokeInvitationCommand command = new RevokeInvitationCommand(
                "token-1",
                Instant.parse("2025-12-31T00:00:00Z")
        );
        RevokeInvitationResult result = useCase.execute(command);
        assertEquals(false, result.isRevoked());
        assertEquals(InvitationStatus.ACCEPTED, repository.saved.get(0).getStatus());
    }

    @Test
    void requiresCommand() {
        RevokeInvitationUseCase useCase = new RevokeInvitationUseCase(new InMemoryInvitationRepository());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    @Test
    void requiresNow() {
        RevokeInvitationUseCase useCase = new RevokeInvitationUseCase(new InMemoryInvitationRepository());
        RevokeInvitationCommand command = new RevokeInvitationCommand("token-1", null);
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
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
        public boolean existsByToken(String token) {
            return findByToken(token).isPresent();
        }

        @Override
        public List<Invitation> findPending() {
            List<Invitation> result = new ArrayList<>();
            for (Invitation invitation : saved) {
                if (invitation.getStatus() == InvitationStatus.PENDING) {
                    result.add(invitation);
                }
            }
            return result;
        }
    }
}
