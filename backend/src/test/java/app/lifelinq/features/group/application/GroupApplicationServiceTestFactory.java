package app.lifelinq.features.group.application;

import app.lifelinq.features.group.domain.Group;
import app.lifelinq.features.group.domain.GroupRepository;
import app.lifelinq.features.group.domain.Invitation;
import app.lifelinq.features.group.domain.InvitationRepository;
import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.group.domain.MembershipRepository;
import app.lifelinq.features.user.application.UserApplicationServiceTestFactory;
import app.lifelinq.features.user.domain.User;
import app.lifelinq.features.user.domain.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class GroupApplicationServiceTestFactory {
    private GroupApplicationServiceTestFactory() {
    }

    public static GroupApplicationService createForContextResolution(MembershipRepository membershipRepository) {
        GroupRepository groupRepository = new StubGroupRepository();
        InvitationRepository invitationRepository = new StubInvitationRepository();
        InvitationTokenGenerator tokenGenerator = () -> "test-token";
        var userService = UserApplicationServiceTestFactory.create(new InMemoryUserRepository());
        return GroupApplicationService.create(
                groupRepository,
                membershipRepository,
                invitationRepository,
                tokenGenerator,
                userService,
                userService,
                Clock.systemUTC()
        );
    }

    private static final class InMemoryUserRepository implements UserRepository {
        private final java.util.Map<UUID, User> users = new java.util.HashMap<>();

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
            users.remove(id);
        }
    }

    private static final class StubGroupRepository implements GroupRepository {
        @Override
        public void save(Group group) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public Optional<Group> findById(UUID id) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public void deleteById(UUID id) {
            throw new UnsupportedOperationException("not used");
        }
    }

    private static final class StubInvitationRepository implements InvitationRepository {
        @Override
        public Optional<Invitation> findByToken(String token) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public Optional<Invitation> findById(UUID id) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public boolean existsByToken(String token) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public List<Invitation> findActive() {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public void save(Invitation invitation) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public Optional<Invitation> findActiveByGroupIdAndInviteeEmail(UUID groupId, String inviteeEmail) {
            throw new UnsupportedOperationException("not used");
        }
    }
}
