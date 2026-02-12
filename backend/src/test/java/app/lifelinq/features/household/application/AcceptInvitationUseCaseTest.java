package app.lifelinq.features.household.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.lifelinq.features.household.domain.HouseholdRole;
import app.lifelinq.features.household.domain.Invitation;
import app.lifelinq.features.household.domain.InvitationRepository;
import app.lifelinq.features.household.domain.InvitationStatus;
import app.lifelinq.features.household.domain.Membership;
import app.lifelinq.features.household.domain.MembershipRepository;
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

        assertEquals(invitation.getHouseholdId(), result.getHouseholdId());
        assertEquals(1, membershipRepository.saved.size());
        assertEquals(HouseholdRole.MEMBER, membershipRepository.saved.get(0).getRole());
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

    private static final class InMemoryMembershipRepository implements MembershipRepository {
        private final List<Membership> saved = new ArrayList<>();

        @Override
        public void save(Membership membership) {
            saved.add(membership);
        }

        @Override
        public List<Membership> findByHouseholdId(UUID householdId) {
            return List.of();
        }

        @Override
        public List<UUID> findHouseholdIdsByUserId(UUID userId) {
            return List.of();
        }

        @Override
        public boolean deleteByHouseholdIdAndUserId(UUID householdId, UUID userId) {
            return false;
        }
    }
}
