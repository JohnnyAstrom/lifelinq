package app.lifelinq.features.auth.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.config.JwtSigner;
import app.lifelinq.features.group.contract.UserDefaultGroupProvisioning;
import app.lifelinq.features.group.contract.UserGroupMembershipLookup;
import app.lifelinq.features.group.contract.UserGroupMembershipSummary;
import app.lifelinq.features.user.application.UserApplicationService;
import app.lifelinq.features.user.application.UserApplicationServiceTestFactory;
import app.lifelinq.features.user.domain.User;
import app.lifelinq.features.user.domain.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuthApplicationServiceProvisioningTest {

    @Test
    void firstLoginProvisioningCreatesMembershipAndSetsActiveGroup() {
        TestFixture fixture = new TestFixture();
        UUID userId = UUID.randomUUID();

        String token = fixture.authApplicationService.ensureProvisionedAndSignToken(userId);

        assertNotNull(token);
        User user = fixture.userRepository.findById(userId).orElseThrow();
        assertNotNull(user.getActiveGroupId());
        var memberships = fixture.userDefaultGroupProvisioning.listMemberships(userId);
        assertEquals(1, memberships.size());
        assertEquals(user.getActiveGroupId(), memberships.get(0).groupId());
        assertEquals("ADMIN", memberships.get(0).role());
        assertTrue(fixture.userDefaultGroupProvisioning.hasProvisionedGroup(userId));
    }

    @Test
    void secondLoginIsIdempotentAndDoesNotCreateDuplicateGroupOrMembership() {
        TestFixture fixture = new TestFixture();
        UUID userId = UUID.randomUUID();

        String firstToken = fixture.authApplicationService.ensureProvisionedAndSignToken(userId);
        UUID firstActiveGroupId = fixture.userRepository.findById(userId).orElseThrow().getActiveGroupId();
        String secondToken = fixture.authApplicationService.ensureProvisionedAndSignToken(userId);
        UUID secondActiveGroupId = fixture.userRepository.findById(userId).orElseThrow().getActiveGroupId();

        assertNotNull(firstToken);
        assertNotNull(secondToken);
        assertEquals(firstActiveGroupId, secondActiveGroupId);
        assertEquals(1, fixture.userDefaultGroupProvisioning.listMemberships(userId).size());
        assertTrue(fixture.userDefaultGroupProvisioning.hasProvisionedGroup(userId));
        assertEquals(2, fixture.userDefaultGroupProvisioning.callsFor(userId));
    }

    private static final class TestFixture {
        private final InMemoryUserRepository userRepository = new InMemoryUserRepository();
        private final FakeUserDefaultGroupProvisioning userDefaultGroupProvisioning = new FakeUserDefaultGroupProvisioning();
        private final AuthApplicationService authApplicationService;

        private TestFixture() {
            UserApplicationService userApplicationService = UserApplicationServiceTestFactory.create(userRepository);
            authApplicationService = new AuthApplicationService(
                    userApplicationService,
                    userApplicationService,
                    userApplicationService,
                    userApplicationService,
                    userApplicationService,
                    userDefaultGroupProvisioning,
                    userDefaultGroupProvisioning,
                    new JwtSigner("test-secret", 300, null, null,
                            Clock.fixed(Instant.parse("2026-02-26T00:00:00Z"), ZoneOffset.UTC))
            );
        }
    }

    private static final class InMemoryUserRepository implements UserRepository {
        private final Map<UUID, User> users = new HashMap<>();

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

    private static final class FakeUserDefaultGroupProvisioning implements
            UserDefaultGroupProvisioning,
            UserGroupMembershipLookup {
        private final Map<UUID, UUID> defaultGroupsByUser = new HashMap<>();
        private final Map<UUID, Integer> callsByUser = new HashMap<>();

        @Override
        public UUID ensureDefaultGroupProvisioned(UUID userId) {
            callsByUser.merge(userId, 1, Integer::sum);
            return defaultGroupsByUser.computeIfAbsent(userId, id ->
                    UUID.nameUUIDFromBytes(("personal-group:" + id).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        }

        @Override
        public boolean isMember(UUID userId, UUID groupId) {
            return groupId != null && groupId.equals(defaultGroupsByUser.get(userId));
        }

        @Override
        public List<UserGroupMembershipSummary> listMemberships(UUID userId) {
            UUID groupId = defaultGroupsByUser.get(userId);
            if (groupId == null) {
                return List.of();
            }
            return List.of(new UserGroupMembershipSummary(groupId, "Personal", "ADMIN"));
        }

        private boolean hasProvisionedGroup(UUID userId) {
            return defaultGroupsByUser.containsKey(userId);
        }

        private int callsFor(UUID userId) {
            return callsByUser.getOrDefault(userId, 0);
        }
    }
}
