package app.lifelinq.features.group.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.group.domain.MembershipRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ResolveGroupForUserUseCaseTest {

    @Test
    void returnsGroupWhenExactlyOneMembership() {
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        ResolveGroupForUserUseCase useCase = new ResolveGroupForUserUseCase(
                new FakeMembershipRepository(userId, List.of(groupId))
        );

        var result = useCase.resolveForUser(userId);

        assertTrue(result.isPresent());
        assertEquals(groupId, result.get());
    }

    @Test
    void returnsEmptyWhenNoMemberships() {
        UUID userId = UUID.randomUUID();
        ResolveGroupForUserUseCase useCase = new ResolveGroupForUserUseCase(
                new FakeMembershipRepository(userId, List.of())
        );

        var result = useCase.resolveForUser(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyWhenMultipleMemberships() {
        UUID userId = UUID.randomUUID();
        ResolveGroupForUserUseCase useCase = new ResolveGroupForUserUseCase(
                new FakeMembershipRepository(userId, List.of(UUID.randomUUID(), UUID.randomUUID()))
        );

        assertThrows(AmbiguousGroupException.class, () -> useCase.resolveForUser(userId));
    }

    private static final class FakeMembershipRepository implements MembershipRepository {
        private final UUID userId;
        private final List<UUID> groupIds;

        private FakeMembershipRepository(UUID userId, List<UUID> groupIds) {
            this.userId = userId;
            this.groupIds = groupIds;
        }

        @Override
        public void save(Membership membership) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public List<Membership> findByGroupId(UUID groupId) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public List<Membership> findByUserId(UUID userId) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public List<UUID> findGroupIdsByUserId(UUID userId) {
            if (!this.userId.equals(userId)) {
                return List.of();
            }
            return groupIds;
        }

        @Override
        public boolean deleteByGroupIdAndUserId(UUID groupId, UUID userId) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public void deleteByUserId(UUID userId) {
            throw new UnsupportedOperationException("not used");
        }
    }
}
