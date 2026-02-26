package app.lifelinq.features.user.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.group.contract.GroupAccountDeletionGovernancePort;
import app.lifelinq.features.group.contract.UserGroupMembershipView;
import app.lifelinq.features.user.domain.User;
import app.lifelinq.features.user.domain.UserRepository;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

class UserApplicationServiceDeleteAccountTest {

    @Test
    void deleteAccountIsTransactionalOnPublicMethod() throws Exception {
        Method method = UserApplicationService.class.getMethod("deleteAccount", UUID.class);

        assertNotNull(method.getAnnotation(Transactional.class));
    }

    @Test
    void deleteAccountOrchestratesInRequiredOrder() {
        RecordingGovernancePort port = new RecordingGovernancePort();
        RecordingUserRepository userRepository = new RecordingUserRepository(port.events);
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        port.memberships = List.of(new UserGroupMembershipView(groupId, false, 1, 0));
        userRepository.save(new User(userId));
        RecordingDeleteAccountUseCase deleteAccountUseCase = new RecordingDeleteAccountUseCase(port, userRepository, port.events);
        UserApplicationService service = new UserApplicationService(
                new EnsureUserExistsUseCase(userRepository),
                deleteAccountUseCase,
                new UpdateUserProfileUseCase(userRepository),
                userRepository
        );

        service.deleteAccount(userId);

        assertEquals(List.of(
                "load-memberships",
                "validate-governance",
                "delete-memberships",
                "delete-empty-groups",
                "delete-user"
        ), port.events);
        assertEquals(userId, port.deletedMembershipsUserId);
        assertEquals(List.of(groupId), port.emptyGroupCandidates);
        assertEquals(userId, userRepository.deletedUserId);
        assertTrue(userRepository.findById(userId).isEmpty());
    }

    private static final class RecordingDeleteAccountUseCase extends DeleteAccountUseCase {
        private final List<String> events;

        private RecordingDeleteAccountUseCase(
                GroupAccountDeletionGovernancePort port,
                UserRepository userRepository,
                List<String> events
        ) {
            super(port, userRepository);
            this.events = events;
        }

        @Override
        void validateGovernance(List<UserGroupMembershipView> memberships) {
            events.add("validate-governance");
            super.validateGovernance(memberships);
        }
    }

    private static final class RecordingGovernancePort implements GroupAccountDeletionGovernancePort {
        private final List<String> events = new ArrayList<>();
        private List<UserGroupMembershipView> memberships = List.of();
        private UUID deletedMembershipsUserId;
        private List<UUID> emptyGroupCandidates = List.of();

        @Override
        public List<UserGroupMembershipView> findMembershipsForUser(UUID userId) {
            events.add("load-memberships");
            return memberships;
        }

        @Override
        public void deleteMembershipsByUserId(UUID userId) {
            events.add("delete-memberships");
            deletedMembershipsUserId = userId;
        }

        @Override
        public void deleteEmptyGroupsByIds(List<UUID> groupIds) {
            events.add("delete-empty-groups");
            emptyGroupCandidates = new ArrayList<>(groupIds);
        }
    }

    private static final class RecordingUserRepository implements UserRepository {
        private final Map<UUID, User> users = new HashMap<>();
        private final List<String> events;
        private UUID deletedUserId;

        private RecordingUserRepository(List<String> events) {
            this.events = events;
        }

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
            events.add("delete-user");
            deletedUserId = id;
            users.remove(id);
        }
    }
}
