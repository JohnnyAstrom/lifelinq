package app.lifelinq.features.group.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.lifelinq.features.group.domain.Group;
import app.lifelinq.features.group.domain.GroupRepository;
import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.group.domain.MembershipRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CreateGroupUseCaseTest {

    @Test
    void createsGroupAndReturnsId() {
        InMemoryGroupRepository groupRepository = new InMemoryGroupRepository();
        InMemoryMembershipRepository membershipRepository = new InMemoryMembershipRepository();
        CreateGroupUseCase useCase = new CreateGroupUseCase(groupRepository, membershipRepository);
        CreateGroupCommand command = new CreateGroupCommand("Home", UUID.randomUUID());

        CreateGroupResult result = useCase.execute(command);

        assertNotNull(result);
        assertNotNull(result.getGroupId());
        assertEquals(1, groupRepository.saved.size());
        assertEquals(1, membershipRepository.saved.size());
    }

    @Test
    void requiresCommand() {
        CreateGroupUseCase useCase = new CreateGroupUseCase(
                new InMemoryGroupRepository(),
                new InMemoryMembershipRepository()
        );
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    @Test
    void requiresOwnerUserId() {
        CreateGroupUseCase useCase = new CreateGroupUseCase(
                new InMemoryGroupRepository(),
                new InMemoryMembershipRepository()
        );
        CreateGroupCommand command = new CreateGroupCommand("Home", null);
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }

    private static final class InMemoryGroupRepository implements GroupRepository {
        private final List<Group> saved = new ArrayList<>();

        @Override
        public void save(Group group) {
            saved.add(group);
        }

        @Override
        public java.util.Optional<Group> findById(UUID id) {
            for (Group group : saved) {
                if (id.equals(group.getId())) {
                    return java.util.Optional.of(group);
                }
            }
            return java.util.Optional.empty();
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
