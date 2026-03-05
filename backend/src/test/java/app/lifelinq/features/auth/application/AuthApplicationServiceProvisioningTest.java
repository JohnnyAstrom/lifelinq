package app.lifelinq.features.auth.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.config.JwtSigner;
import app.lifelinq.features.auth.domain.AuthIdentity;
import app.lifelinq.features.auth.domain.AuthIdentityRepository;
import app.lifelinq.features.auth.domain.AuthMailSender;
import app.lifelinq.features.auth.domain.AuthProvider;
import app.lifelinq.features.auth.domain.MagicLinkChallenge;
import app.lifelinq.features.auth.domain.MagicLinkChallengeRepository;
import app.lifelinq.features.auth.domain.MagicLinkTokenGenerator;
import app.lifelinq.features.auth.domain.RefreshSession;
import app.lifelinq.features.auth.domain.RefreshSessionRepository;
import app.lifelinq.features.auth.domain.RefreshToken;
import app.lifelinq.features.auth.domain.RefreshTokenGenerator;
import app.lifelinq.features.auth.domain.RefreshTokenHasher;
import app.lifelinq.features.auth.domain.RefreshTokenRepository;
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

    @Test
    void provisioningPassesInitialPlaceNameToGroupProvisioningContract() {
        TestFixture fixture = new TestFixture();
        UUID userId = UUID.randomUUID();

        fixture.authApplicationService.ensureProvisionedAndSignToken(userId, "My Place");

        assertEquals("My Place", fixture.userDefaultGroupProvisioning.lastInitialPlaceNameFor(userId));
    }

    @Test
    void firstOAuthLoginCreatesIdentityAndUserAndReturnsAuthPair() {
        TestFixture fixture = new TestFixture();

        AuthTokenPair tokens = fixture.authApplicationService.issueAuthPairForOAuthLogin(
                "google",
                "oauth-subject",
                "user@example.com",
                true
        );

        assertNotNull(tokens.accessToken());
        assertNotNull(tokens.refreshToken());

        AuthIdentity identity = fixture.authIdentityRepository
                .findByProviderAndSubject(AuthProvider.GOOGLE, "oauth-subject")
                .orElseThrow();
        assertTrue(fixture.userRepository.findById(identity.getUserId()).isPresent());
    }

    @Test
    void secondOAuthLoginReusesExistingIdentityUserId() {
        TestFixture fixture = new TestFixture();

        fixture.authApplicationService.issueAuthPairForOAuthLogin(
                "google",
                "oauth-subject",
                "user@example.com",
                true
        );
        UUID firstUserId = fixture.authIdentityRepository
                .findByProviderAndSubject(AuthProvider.GOOGLE, "oauth-subject")
                .orElseThrow()
                .getUserId();

        fixture.authApplicationService.issueAuthPairForOAuthLogin(
                "google",
                "oauth-subject",
                "other@example.com",
                true
        );
        UUID secondUserId = fixture.authIdentityRepository
                .findByProviderAndSubject(AuthProvider.GOOGLE, "oauth-subject")
                .orElseThrow()
                .getUserId();

        assertEquals(firstUserId, secondUserId);
        assertEquals(1, fixture.userRepository.count());
    }

    @Test
    void verifiedEmailLinksOAuthIdentityToExistingEmailIdentityUser() {
        TestFixture fixture = new TestFixture();
        UUID existingUserId = UUID.randomUUID();
        fixture.authIdentityRepository.save(new AuthIdentity(
                UUID.randomUUID(),
                AuthProvider.EMAIL,
                "user@example.com",
                existingUserId
        ));

        fixture.authApplicationService.issueAuthPairForOAuthLogin(
                "google",
                "oauth-subject",
                " User@example.com ",
                true
        );

        UUID oauthUserId = fixture.authIdentityRepository
                .findByProviderAndSubject(AuthProvider.GOOGLE, "oauth-subject")
                .orElseThrow()
                .getUserId();
        assertEquals(existingUserId, oauthUserId);
    }

    @Test
    void unverifiedEmailDoesNotLinkToExistingEmailIdentityUser() {
        TestFixture fixture = new TestFixture();
        UUID existingUserId = UUID.randomUUID();
        fixture.authIdentityRepository.save(new AuthIdentity(
                UUID.randomUUID(),
                AuthProvider.EMAIL,
                "user@example.com",
                existingUserId
        ));

        fixture.authApplicationService.issueAuthPairForOAuthLogin(
                "apple",
                "oauth-subject",
                "user@example.com",
                false
        );

        UUID oauthUserId = fixture.authIdentityRepository
                .findByProviderAndSubject(AuthProvider.APPLE, "oauth-subject")
                .orElseThrow()
                .getUserId();
        assertNotEquals(existingUserId, oauthUserId);
        assertTrue(fixture.userRepository.findById(oauthUserId).isPresent());
    }

    @Test
    void devLoginReusesExistingUserByEmail() {
        TestFixture fixture = new TestFixture();
        UUID existingUserId = UUID.randomUUID();
        fixture.userRepository.save(new User(existingUserId, null, "user@example.com", null, null));

        AuthTokenPair tokens = fixture.authApplicationService.devLogin(" User@example.com ");

        assertNotNull(tokens.accessToken());
        assertNotNull(tokens.refreshToken());
        assertEquals(1, fixture.userRepository.count());
        assertTrue(fixture.userRepository.findById(existingUserId).isPresent());
    }

    @Test
    void rejectsMagicLinkTtlAboveConfiguredMaximum() {
        UserApplicationService userApplicationService = UserApplicationServiceTestFactory.create(new InMemoryUserRepository());
        FakeUserDefaultGroupProvisioning provisioning = new FakeUserDefaultGroupProvisioning();

        assertThrows(IllegalArgumentException.class, () -> new AuthApplicationService(
                userApplicationService,
                userApplicationService,
                userApplicationService,
                userApplicationService,
                userApplicationService,
                provisioning,
                provisioning,
                new InMemoryAuthIdentityRepository(),
                new InMemoryMagicLinkChallengeRepository(),
                () -> "magic-token",
                (email, verifyUrl) -> {
                },
                new InMemoryRefreshSessionRepository(),
                new InMemoryRefreshTokenRepository(),
                () -> "refresh-token",
                new PassthroughRefreshTokenHasher(),
                new JwtSigner("test-secret", 300, null, null,
                        Clock.fixed(Instant.parse("2026-02-26T00:00:00Z"), ZoneOffset.UTC)),
                Clock.fixed(Instant.parse("2026-02-26T00:00:00Z"), ZoneOffset.UTC),
                java.time.Duration.ofHours(2),
                java.time.Duration.ofHours(1),
                java.time.Duration.ofDays(30),
                java.time.Duration.ofDays(90),
                "http://localhost:8080/auth/magic/verify",
                "mobileapp://auth/complete"
        ));
    }

    private static final class TestFixture {
        private final InMemoryUserRepository userRepository = new InMemoryUserRepository();
        private final FakeUserDefaultGroupProvisioning userDefaultGroupProvisioning = new FakeUserDefaultGroupProvisioning();
        private final InMemoryAuthIdentityRepository authIdentityRepository = new InMemoryAuthIdentityRepository();
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
                    authIdentityRepository,
                    new InMemoryMagicLinkChallengeRepository(),
                    () -> "magic-token",
                    (email, verifyUrl) -> {
                    },
                    new InMemoryRefreshSessionRepository(),
                    new InMemoryRefreshTokenRepository(),
                    () -> "refresh-token",
                    new PassthroughRefreshTokenHasher(),
                    new JwtSigner("test-secret", 300, null, null,
                            Clock.fixed(Instant.parse("2026-02-26T00:00:00Z"), ZoneOffset.UTC)),
                    Clock.fixed(Instant.parse("2026-02-26T00:00:00Z"), ZoneOffset.UTC),
                    java.time.Duration.ofMinutes(15),
                    java.time.Duration.ofHours(1),
                    java.time.Duration.ofDays(30),
                    java.time.Duration.ofDays(90),
                    "http://localhost:8080/auth/magic/verify",
                    "mobileapp://auth/complete"
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
        public Optional<User> findByEmail(String email) {
            return users.values().stream()
                    .filter(user -> email != null && email.equals(user.getEmail()))
                    .findFirst();
        }

        @Override
        public void save(User user) {
            users.put(user.getId(), user);
        }

        @Override
        public void deleteById(UUID id) {
            users.remove(id);
        }

        private int count() {
            return users.size();
        }
    }

    private static final class FakeUserDefaultGroupProvisioning implements
            UserDefaultGroupProvisioning,
            UserGroupMembershipLookup {
        private final Map<UUID, UUID> defaultGroupsByUser = new HashMap<>();
        private final Map<UUID, Integer> callsByUser = new HashMap<>();
        private final Map<UUID, String> initialPlaceNamesByUser = new HashMap<>();

        @Override
        public UUID ensureDefaultGroupProvisioned(UUID userId, String initialPlaceName) {
            callsByUser.merge(userId, 1, Integer::sum);
            initialPlaceNamesByUser.put(userId, initialPlaceName);
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

        private String lastInitialPlaceNameFor(UUID userId) {
            return initialPlaceNamesByUser.get(userId);
        }
    }

    private static final class InMemoryAuthIdentityRepository implements AuthIdentityRepository {
        private final Map<String, AuthIdentity> byKey = new HashMap<>();

        @Override
        public Optional<AuthIdentity> findByProviderAndSubject(AuthProvider provider, String subject) {
            return Optional.ofNullable(byKey.get(provider.name() + ":" + subject));
        }

        @Override
        public void save(AuthIdentity identity) {
            byKey.put(identity.getProvider().name() + ":" + identity.getSubject(), identity);
        }
    }

    private static final class InMemoryMagicLinkChallengeRepository implements MagicLinkChallengeRepository {
        private final Map<String, MagicLinkChallenge> byToken = new HashMap<>();

        @Override
        public Optional<MagicLinkChallenge> findByToken(String token) {
            return Optional.ofNullable(byToken.get(token));
        }

        @Override
        public boolean existsByToken(String token) {
            return byToken.containsKey(token);
        }

        @Override
        public void save(MagicLinkChallenge challenge) {
            byToken.put(challenge.getToken(), challenge);
        }
    }

    private static final class InMemoryRefreshSessionRepository implements RefreshSessionRepository {
        private final Map<UUID, RefreshSession> byId = new HashMap<>();

        @Override
        public void save(RefreshSession refreshSession) {
            byId.put(refreshSession.getId(), refreshSession);
        }

        @Override
        public Optional<RefreshSession> findById(UUID id) {
            return Optional.ofNullable(byId.get(id));
        }
    }

    private static final class InMemoryRefreshTokenRepository implements RefreshTokenRepository {
        private final Map<UUID, RefreshToken> byId = new HashMap<>();
        private final Map<String, UUID> idByHash = new HashMap<>();

        @Override
        public void save(RefreshToken refreshToken) {
            byId.put(refreshToken.getId(), refreshToken);
            idByHash.put(refreshToken.getTokenHash(), refreshToken.getId());
        }

        @Override
        public Optional<RefreshToken> findById(UUID id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<RefreshToken> findByTokenHash(String tokenHash) {
            UUID id = idByHash.get(tokenHash);
            if (id == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(byId.get(id));
        }
    }

    private static final class PassthroughRefreshTokenHasher implements RefreshTokenHasher {
        @Override
        public String hash(String plaintextToken) {
            return plaintextToken;
        }

        @Override
        public boolean matches(String plaintextToken, String hashedToken) {
            return plaintextToken.equals(hashedToken);
        }
    }
}
