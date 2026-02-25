package app.lifelinq.features.group.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.lifelinq.features.group.domain.Invitation;
import app.lifelinq.features.group.domain.InvitationRepository;
import app.lifelinq.features.group.domain.InvitationStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ExpireInvitationsUseCaseTest {

    @Test
    void expiresOnlyPastDueInvitations() {
        InMemoryInvitationRepository repository = new InMemoryInvitationRepository();
        repository.saved.add(Invitation.createActive(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "a@example.com",
                "token-1",
                Instant.parse("2025-01-01T00:00:00Z")
        ));
        repository.saved.add(Invitation.createActive(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "b@example.com",
                "token-2",
                Instant.parse("2027-01-01T00:00:00Z")
        ));

        ExpireInvitationsUseCase useCase = new ExpireInvitationsUseCase(repository);
        ExpireInvitationsCommand command = new ExpireInvitationsCommand(Instant.parse("2026-01-01T00:00:00Z"));

        ExpireInvitationsResult result = useCase.execute(command);

        assertEquals(1, result.getExpiredCount());
        assertEquals(InvitationStatus.ACTIVE, repository.saved.get(0).getStatus());
        assertEquals(InvitationStatus.ACTIVE, repository.saved.get(1).getStatus());
    }

    @Test
    void requiresCommand() {
        ExpireInvitationsUseCase useCase = new ExpireInvitationsUseCase(new InMemoryInvitationRepository());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    @Test
    void requiresNow() {
        ExpireInvitationsUseCase useCase = new ExpireInvitationsUseCase(new InMemoryInvitationRepository());
        ExpireInvitationsCommand command = new ExpireInvitationsCommand(null);
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
}
