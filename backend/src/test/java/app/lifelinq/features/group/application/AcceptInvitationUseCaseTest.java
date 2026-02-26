package app.lifelinq.features.group.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.lifelinq.features.group.domain.GroupRole;
import app.lifelinq.features.group.domain.Invitation;
import app.lifelinq.features.group.domain.InvitationRepository;
import app.lifelinq.features.group.domain.InvitationStatus;
import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.group.domain.MembershipRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AcceptInvitationUseCaseTest {

    @Test
    void acceptsInvitationAndCreatesMembership() {
        InMemoryInvitationRepository invitationRepository = new InMemoryInvitationRepository();
        InMemoryMembershipRepository membershipRepository = new InMemoryMembershipRepository();
        Invitation invitation = Invitation.createActive(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "test@example.com",
                "token-1",
                Instant.parse("2026-01-01T00:00:00Z")
        );
        invitationRepository.save(invitation);

        AcceptInvitationUseCase useCase = new AcceptInvitationUseCase(invitationRepository, membershipRepository);
        AcceptInvitationCommand command = new AcceptInvitationCommand(
                "token-1",
                UUID.randomUUID(),
                Instant.parse("2025-12-31T00:00:00Z")
        );

        AcceptInvitationResult result = useCase.execute(command);

        assertEquals(invitation.getGroupId(), result.getGroupId());
        assertEquals(1, membershipRepository.saved.size());
        assertEquals(GroupRole.MEMBER, membershipRepository.saved.get(0).getRole());
        assertEquals(InvitationStatus.REVOKED, invitationRepository.saved.get(0).getStatus());
    }

    @Test
    void requiresCommand() {
        AcceptInvitationUseCase useCase = new AcceptInvitationUseCase(
                new InMemoryInvitationRepository(),
                new InMemoryMembershipRepository()
        );
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    @Test
    void failsWhenInvitationMissing() {
        AcceptInvitationUseCase useCase = new AcceptInvitationUseCase(
                new InMemoryInvitationRepository(),
                new InMemoryMembershipRepository()
        );
        AcceptInvitationCommand command = new AcceptInvitationCommand(
                "missing",
                UUID.randomUUID(),
                Instant.parse("2025-12-31T00:00:00Z")
        );
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }

    @Test
    void repeatedAcceptIsIdempotentWhenMembershipAlreadyExists() {
        InMemoryInvitationRepository invitationRepository = new InMemoryInvitationRepository();
        InMemoryMembershipRepository membershipRepository = new InMemoryMembershipRepository();
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Invitation invitation = Invitation.createActive(
                UUID.randomUUID(),
                groupId,
                "test@example.com",
                "token-repeat",
                Instant.parse("2026-01-01T00:00:00Z")
        );
        invitationRepository.save(invitation);

        AcceptInvitationUseCase useCase = new AcceptInvitationUseCase(invitationRepository, membershipRepository);
        AcceptInvitationCommand command = new AcceptInvitationCommand(
                "token-repeat",
                userId,
                Instant.parse("2025-12-31T00:00:00Z")
        );

        useCase.execute(command);
        AcceptInvitationResult result = useCase.execute(command);

        assertEquals(groupId, result.getGroupId());
        assertEquals(1, membershipRepository.findByGroupId(groupId).size());
        assertEquals(InvitationStatus.REVOKED, invitationRepository.findByToken("token-repeat").orElseThrow().getStatus());
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

    private static final class InMemoryMembershipRepository implements MembershipRepository {
        private final List<Membership> saved = new ArrayList<>();

        @Override
        public void save(Membership membership) {
            saved.add(membership);
        }

        @Override
        public List<Membership> findByGroupId(UUID groupId) {
            List<Membership> result = new ArrayList<>();
            for (Membership membership : saved) {
                if (membership.getGroupId().equals(groupId)) {
                    result.add(membership);
                }
            }
            return result;
        }

        @Override
        public List<Membership> findByUserId(UUID userId) {
            return List.of();
        }

        @Override
        public List<UUID> findGroupIdsByUserId(UUID userId) {
            return List.of();
        }

        @Override
        public boolean deleteByGroupIdAndUserId(UUID groupId, UUID userId) {
            return false;
        }

        @Override
        public void deleteByUserId(UUID userId) {
        }
    }
}
