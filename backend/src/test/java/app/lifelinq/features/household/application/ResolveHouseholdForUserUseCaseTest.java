package app.lifelinq.features.household.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.household.domain.Membership;
import app.lifelinq.features.household.domain.MembershipRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ResolveHouseholdForUserUseCaseTest {

    @Test
    void returnsHouseholdWhenExactlyOneMembership() {
        UUID userId = UUID.randomUUID();
        UUID householdId = UUID.randomUUID();
        ResolveHouseholdForUserUseCase useCase = new ResolveHouseholdForUserUseCase(
                new FakeMembershipRepository(userId, List.of(householdId))
        );

        var result = useCase.resolveForUser(userId);

        assertTrue(result.isPresent());
        assertEquals(householdId, result.get());
    }

    @Test
    void returnsEmptyWhenNoMemberships() {
        UUID userId = UUID.randomUUID();
        ResolveHouseholdForUserUseCase useCase = new ResolveHouseholdForUserUseCase(
                new FakeMembershipRepository(userId, List.of())
        );

        var result = useCase.resolveForUser(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyWhenMultipleMemberships() {
        UUID userId = UUID.randomUUID();
        ResolveHouseholdForUserUseCase useCase = new ResolveHouseholdForUserUseCase(
                new FakeMembershipRepository(userId, List.of(UUID.randomUUID(), UUID.randomUUID()))
        );

        assertThrows(AmbiguousHouseholdException.class, () -> useCase.resolveForUser(userId));
    }

    private static final class FakeMembershipRepository implements MembershipRepository {
        private final UUID userId;
        private final List<UUID> householdIds;

        private FakeMembershipRepository(UUID userId, List<UUID> householdIds) {
            this.userId = userId;
            this.householdIds = householdIds;
        }

        @Override
        public void save(Membership membership) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public List<Membership> findByHouseholdId(UUID householdId) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public List<UUID> findHouseholdIdsByUserId(UUID userId) {
            if (!this.userId.equals(userId)) {
                return List.of();
            }
            return householdIds;
        }

        @Override
        public boolean deleteByHouseholdIdAndUserId(UUID householdId, UUID userId) {
            throw new UnsupportedOperationException("not used");
        }
    }
}
