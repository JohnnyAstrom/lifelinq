package app.lifelinq.features.group.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.group.domain.GroupRole;
import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.group.domain.MembershipRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RemoveMemberFromGroupUseCaseTest {

    @Test
    void removesMemberWhenPresent() {
        InMemoryMembershipRepository repository = new InMemoryMembershipRepository();
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        repository.save(new Membership(groupId, userId, GroupRole.MEMBER));

        RemoveMemberFromGroupUseCase useCase = new RemoveMemberFromGroupUseCase(repository);
        RemoveMemberFromGroupCommand command = new RemoveMemberFromGroupCommand(groupId, userId);

        RemoveMemberFromGroupResult result = useCase.execute(command);

        assertTrue(result.isRemoved());
        assertEquals(0, repository.saved.size());
    }

    @Test
    void returnsFalseWhenNoMatch() {
        InMemoryMembershipRepository repository = new InMemoryMembershipRepository();
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        RemoveMemberFromGroupUseCase useCase = new RemoveMemberFromGroupUseCase(repository);
        RemoveMemberFromGroupCommand command = new RemoveMemberFromGroupCommand(groupId, userId);

        RemoveMemberFromGroupResult result = useCase.execute(command);

        assertFalse(result.isRemoved());
    }

    @Test
    void requiresCommand() {
        RemoveMemberFromGroupUseCase useCase = new RemoveMemberFromGroupUseCase(new InMemoryMembershipRepository());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    @Test
    void requiresGroupId() {
        RemoveMemberFromGroupUseCase useCase = new RemoveMemberFromGroupUseCase(new InMemoryMembershipRepository());
        RemoveMemberFromGroupCommand command = new RemoveMemberFromGroupCommand(null, UUID.randomUUID());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }

    @Test
    void requiresUserId() {
        RemoveMemberFromGroupUseCase useCase = new RemoveMemberFromGroupUseCase(new InMemoryMembershipRepository());
        RemoveMemberFromGroupCommand command = new RemoveMemberFromGroupCommand(UUID.randomUUID(), null);
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
