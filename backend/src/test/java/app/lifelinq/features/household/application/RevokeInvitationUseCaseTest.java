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
        Invitation invitation = Invitation.createActive(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "test@example.com",
                "token-1",
                Instant.parse("2026-01-01T00:00:00Z")
        );
        repository.save(invitation);

        RevokeInvitationUseCase useCase = new RevokeInvitationUseCase(repository);
        RevokeInvitationCommand command = new RevokeInvitationCommand(
                invitation.getId(),
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
                UUID.randomUUID(),
                Instant.parse("2025-12-31T00:00:00Z")
        );
        RevokeInvitationResult result = useCase.execute(command);
        assertEquals(false, result.isRevoked());
    }

    @Test
    void requiresCommand() {
        RevokeInvitationUseCase useCase = new RevokeInvitationUseCase(new InMemoryInvitationRepository());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    @Test
    void requiresNow() {
        RevokeInvitationUseCase useCase = new RevokeInvitationUseCase(new InMemoryInvitationRepository());
        RevokeInvitationCommand command = new RevokeInvitationCommand(UUID.randomUUID(), null);
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
        public Optional<Invitation> findActiveByHouseholdIdAndInviteeEmail(UUID householdId, String inviteeEmail) {
            for (Invitation invitation : saved) {
                if (invitation.getStatus() == InvitationStatus.ACTIVE
                        && householdId.equals(invitation.getHouseholdId())
                        && inviteeEmail.equals(invitation.getInviteeEmail())) {
                    return Optional.of(invitation);
                }
            }
            return Optional.empty();
        }
    }
}
