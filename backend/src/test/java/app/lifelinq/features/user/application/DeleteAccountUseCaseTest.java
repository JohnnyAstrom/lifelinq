package app.lifelinq.features.user.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.lifelinq.features.group.contract.GroupAccountDeletionGovernancePort;
import app.lifelinq.features.group.contract.UserGroupMembershipView;
import app.lifelinq.features.user.contract.DeleteAccountBlockedException;
import app.lifelinq.features.user.domain.User;
import app.lifelinq.features.user.domain.UserRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DeleteAccountUseCaseTest {

    @Test
    void blocksWhenUserIsSoleAdminInGroupWithOtherMembers() {
        RecordingPort port = new RecordingPort();
        port.memberships = List.of(new UserGroupMembershipView(UUID.randomUUID(), true, 2, 1));
        DeleteAccountUseCase useCase = new DeleteAccountUseCase(port, new InMemoryUserRepository());

        assertThrows(
                DeleteAccountBlockedException.class,
                () -> useCase.validateGovernance(useCase.loadMemberships(UUID.randomUUID()))
        );
    }

    @Test
    void allowsWhenGroupHasAnotherAdmin() {
        RecordingPort port = new RecordingPort();
        port.memberships = List.of(new UserGroupMembershipView(UUID.randomUUID(), true, 3, 2));
        DeleteAccountUseCase useCase = new DeleteAccountUseCase(port, new InMemoryUserRepository());

        useCase.validateGovernance(useCase.loadMemberships(UUID.randomUUID()));
    }

    @Test
    void allowsWhenUserIsMemberOnly() {
        RecordingPort port = new RecordingPort();
        port.memberships = List.of(new UserGroupMembershipView(UUID.randomUUID(), false, 2, 1));
        DeleteAccountUseCase useCase = new DeleteAccountUseCase(port, new InMemoryUserRepository());

        useCase.validateGovernance(useCase.loadMemberships(UUID.randomUUID()));
    }

    @Test
    void mixedRolesAcrossThreeGroupsBlocksWhenAnyGroupHasSoleAdminWithOtherMembers() {
        RecordingPort port = new RecordingPort();
        port.memberships = List.of(
                new UserGroupMembershipView(UUID.randomUUID(), false, 4, 1),
                new UserGroupMembershipView(UUID.randomUUID(), true, 3, 2),
                new UserGroupMembershipView(UUID.randomUUID(), true, 2, 1)
        );
        DeleteAccountUseCase useCase = new DeleteAccountUseCase(port, new InMemoryUserRepository());

        assertThrows(
                DeleteAccountBlockedException.class,
                () -> useCase.validateGovernance(useCase.loadMemberships(UUID.randomUUID()))
        );
    }

    @Test
    void mixedRolesAcrossThreeGroupsAllowsWhenNoBlockingSoleAdminCaseExists() {
        RecordingPort port = new RecordingPort();
        port.memberships = List.of(
                new UserGroupMembershipView(UUID.randomUUID(), false, 4, 1),
                new UserGroupMembershipView(UUID.randomUUID(), true, 3, 2),
                new UserGroupMembershipView(UUID.randomUUID(), true, 1, 1)
        );
        DeleteAccountUseCase useCase = new DeleteAccountUseCase(port, new InMemoryUserRepository());

        useCase.validateGovernance(useCase.loadMemberships(UUID.randomUUID()));
    }

    @Test
    void delegatesDeletionStepsToPortAndUserRepository() {
        RecordingPort port = new RecordingPort();
        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        UUID userId = UUID.randomUUID();
        UUID groupA = UUID.randomUUID();
        UUID groupB = UUID.randomUUID();
        port.memberships = List.of(
                new UserGroupMembershipView(groupA, false, 1, 0),
                new UserGroupMembershipView(groupB, false, 2, 1)
        );
        userRepository.save(new User(userId));
        DeleteAccountUseCase useCase = new DeleteAccountUseCase(port, userRepository);

        List<UserGroupMembershipView> memberships = useCase.loadMemberships(userId);
        useCase.deleteMemberships(userId);
        useCase.deleteEmptyGroups(memberships);
        useCase.deleteUser(userId);

        assertEquals(userId, port.deletedMembershipsForUserId);
        assertEquals(List.of(groupA, groupB), port.emptyGroupCandidates);
        assertEquals(userId, userRepository.deletedUserId);
    }

    private static final class RecordingPort implements GroupAccountDeletionGovernancePort {
        private List<UserGroupMembershipView> memberships = List.of();
        private UUID deletedMembershipsForUserId;
        private List<UUID> emptyGroupCandidates = List.of();

        @Override
        public List<UserGroupMembershipView> findMembershipsForUser(UUID userId) {
            return memberships;
        }

        @Override
        public void deleteMembershipsByUserId(UUID userId) {
            deletedMembershipsForUserId = userId;
        }

        @Override
        public void deleteEmptyGroupsByIds(List<UUID> groupIds) {
            emptyGroupCandidates = new ArrayList<>(groupIds);
        }
    }

    private static final class InMemoryUserRepository implements UserRepository {
        private final Map<UUID, User> users = new HashMap<>();
        private UUID deletedUserId;

        @Override
        public Optional<User> findById(UUID id) {
            return Optional.ofNullable(users.get(id));
        }

        @Override
        public void save(User user) {
            users.put(user.getId(), user);
        }

        @Override
        public void deleteById(UUID id) {
            deletedUserId = id;
            users.remove(id);
        }
    }
}
