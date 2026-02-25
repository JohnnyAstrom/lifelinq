package app.lifelinq.features.group.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.lifelinq.features.group.domain.GroupRole;
import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.group.domain.MembershipRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ListGroupMembersUseCaseTest {

    @Test
    void returnsOnlyMembersForGroup() {
        InMemoryMembershipRepository repository = new InMemoryMembershipRepository();
        ListGroupMembersUseCase useCase = new ListGroupMembersUseCase(repository);
        UUID groupId = UUID.randomUUID();
        UUID otherGroupId = UUID.randomUUID();

        repository.save(new Membership(groupId, UUID.randomUUID(), GroupRole.MEMBER));
        repository.save(new Membership(groupId, UUID.randomUUID(), GroupRole.ADMIN));
        repository.save(new Membership(otherGroupId, UUID.randomUUID(), GroupRole.MEMBER));

        ListGroupMembersCommand command = new ListGroupMembersCommand(groupId);

        ListGroupMembersResult result = useCase.execute(command);

        assertEquals(2, result.getMembers().size());
        assertEquals(groupId, result.getMembers().get(0).getGroupId());
        assertEquals(groupId, result.getMembers().get(1).getGroupId());
    }

    @Test
    void requiresCommand() {
        ListGroupMembersUseCase useCase = new ListGroupMembersUseCase(new InMemoryMembershipRepository());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    @Test
    void requiresGroupId() {
        ListGroupMembersUseCase useCase = new ListGroupMembersUseCase(new InMemoryMembershipRepository());
        ListGroupMembersCommand command = new ListGroupMembersCommand(null);
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }

    @Test
    void requiresMembershipRepository() {
        assertThrows(IllegalArgumentException.class, () -> new ListGroupMembersUseCase(null));
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
                if (groupId.equals(membership.getGroupId())) {
                    result.add(membership);
                }
            }
            return result;
        }

        @Override
        public List<UUID> findGroupIdsByUserId(UUID userId) {
            List<UUID> result = new ArrayList<>();
            for (Membership membership : saved) {
                if (userId.equals(membership.getUserId()) && !result.contains(membership.getGroupId())) {
                    result.add(membership.getGroupId());
                }
            }
            return result;
        }

        @Override
        public List<Membership> findByUserId(UUID userId) {
            List<Membership> result = new ArrayList<>();
            for (Membership membership : saved) {
                if (userId.equals(membership.getUserId())) {
                    result.add(membership);
                }
            }
            return result;
        }

        @Override
        public boolean deleteByGroupIdAndUserId(UUID groupId, UUID userId) {
            for (int i = 0; i < saved.size(); i++) {
                Membership membership = saved.get(i);
                if (groupId.equals(membership.getGroupId()) && userId.equals(membership.getUserId())) {
                    saved.remove(i);
                    return true;
                }
            }
            return false;
        }

        @Override
        public void deleteByUserId(UUID userId) {
            saved.removeIf(membership -> userId.equals(membership.getUserId()));
        }
    }
}
