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

class AddMemberToGroupUseCaseTest {

    @Test
    void addsMemberWithMemberRole() {
        InMemoryMembershipRepository repository = new InMemoryMembershipRepository();
        AddMemberToGroupUseCase useCase = new AddMemberToGroupUseCase(repository);
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AddMemberToGroupCommand command = new AddMemberToGroupCommand(groupId, userId);

        AddMemberToGroupResult result = useCase.execute(command);

        assertEquals(groupId, result.getGroupId());
        assertEquals(userId, result.getUserId());
        assertEquals(GroupRole.MEMBER, result.getRole());
        assertEquals(1, repository.saved.size());
    }

    @Test
    void requiresCommand() {
        AddMemberToGroupUseCase useCase = new AddMemberToGroupUseCase(new InMemoryMembershipRepository());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    @Test
    void requiresGroupId() {
        AddMemberToGroupUseCase useCase = new AddMemberToGroupUseCase(new InMemoryMembershipRepository());
        AddMemberToGroupCommand command = new AddMemberToGroupCommand(null, UUID.randomUUID());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }

    @Test
    void requiresUserId() {
        AddMemberToGroupUseCase useCase = new AddMemberToGroupUseCase(new InMemoryMembershipRepository());
        AddMemberToGroupCommand command = new AddMemberToGroupCommand(UUID.randomUUID(), null);
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
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
    }
}
