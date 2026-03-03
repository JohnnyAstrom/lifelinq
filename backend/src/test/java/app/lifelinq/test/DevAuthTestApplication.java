package app.lifelinq.test;

import app.lifelinq.config.RequestContextConfig;
import app.lifelinq.config.SecurityConfig;
import app.lifelinq.features.auth.api.DevTokenController;
import app.lifelinq.features.auth.api.MeController;
import app.lifelinq.features.auth.domain.AuthIdentityRepository;
import app.lifelinq.features.auth.domain.AuthMailSender;
import app.lifelinq.features.auth.domain.MagicLinkChallengeRepository;
import app.lifelinq.features.auth.domain.MagicLinkTokenGenerator;
import app.lifelinq.features.auth.domain.RefreshSession;
import app.lifelinq.features.auth.domain.RefreshSessionRepository;
import app.lifelinq.features.auth.domain.RefreshToken;
import app.lifelinq.features.auth.domain.RefreshTokenGenerator;
import app.lifelinq.features.auth.domain.RefreshTokenHasher;
import app.lifelinq.features.auth.domain.RefreshTokenRepository;
import app.lifelinq.features.auth.infrastructure.AuthApplicationConfig;
import app.lifelinq.features.auth.infrastructure.HmacSha256RefreshTokenHasher;
import app.lifelinq.features.auth.infrastructure.InMemoryAuthIdentityRepository;
import app.lifelinq.features.auth.infrastructure.InMemoryMagicLinkChallengeRepository;
import app.lifelinq.features.auth.infrastructure.SecureRefreshTokenGenerator;
import app.lifelinq.features.group.application.GroupApplicationService;
import app.lifelinq.features.group.application.GroupApplicationServiceTestFactory;
import app.lifelinq.features.group.contract.GroupAccountDeletionGovernancePort;
import app.lifelinq.features.group.contract.UserDefaultGroupProvisioning;
import app.lifelinq.features.group.contract.UserGroupMembershipLookup;
import app.lifelinq.features.group.contract.UserGroupMembershipSummary;
import app.lifelinq.features.group.contract.UserGroupMembershipView;
import app.lifelinq.features.group.domain.MembershipRepository;
import app.lifelinq.features.group.infrastructure.InMemoryMembershipRepository;
import app.lifelinq.features.user.application.UserApplicationConfig;
import app.lifelinq.features.user.domain.User;
import app.lifelinq.features.user.domain.UserRepository;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import({
        RequestContextConfig.class,
        SecurityConfig.class,
        DevTokenController.class,
        MeController.class,
        AuthApplicationConfig.class,
        UserApplicationConfig.class
})
public class DevAuthTestApplication {

    @Bean
    public MembershipRepository membershipRepository() {
        return new InMemoryMembershipRepository();
    }

    @Bean
    public GroupApplicationService groupApplicationService(
            MembershipRepository membershipRepository
    ) {
        return GroupApplicationServiceTestFactory.createForContextResolution(membershipRepository);
    }

    @Bean
    public UserRepository userRepository() {
        return new UserRepository() {
            private final Map<UUID, User> users = new ConcurrentHashMap<>();

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
        };
    }

    @Bean
    public GroupAccountDeletionGovernancePort groupAccountDeletionGovernancePort() {
        return new GroupAccountDeletionGovernancePort() {
            @Override
            public List<UserGroupMembershipView> findMembershipsForUser(UUID userId) {
                return List.of();
            }

            @Override
            public void deleteMembershipsByUserId(UUID userId) {
            }

            @Override
            public void deleteEmptyGroupsByIds(List<UUID> groupIds) {
            }
        };
    }

    @Bean
    public UserGroupMembershipLookup userGroupMembershipLookup() {
        return new UserGroupMembershipLookup() {
            @Override
            public boolean isMember(UUID userId, UUID groupId) {
                return false;
            }

            @Override
            public List<UserGroupMembershipSummary> listMemberships(UUID userId) {
                return List.of();
            }
        };
    }

    @Bean
    public UserDefaultGroupProvisioning userDefaultGroupProvisioning() {
        return (userId, initialPlaceName) -> UUID.nameUUIDFromBytes(("personal-group:" + userId)
                .getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    @Bean
    public AuthIdentityRepository authIdentityRepository() {
        return new InMemoryAuthIdentityRepository();
    }

    @Bean
    public MagicLinkChallengeRepository magicLinkChallengeRepository() {
        return new InMemoryMagicLinkChallengeRepository();
    }

    @Bean
    public MagicLinkTokenGenerator magicLinkTokenGenerator() {
        return () -> UUID.randomUUID().toString();
    }

    @Bean
    public AuthMailSender authMailSender() {
        return (email, verifyUrl) -> {
        };
    }

    @Bean
    public RefreshTokenGenerator refreshTokenGenerator() {
        return new SecureRefreshTokenGenerator();
    }

    @Bean
    public RefreshTokenHasher refreshTokenHasher() {
        return new HmacSha256RefreshTokenHasher("test-refresh-secret");
    }

    @Bean
    public RefreshSessionRepository refreshSessionRepository() {
        return new RefreshSessionRepository() {
            private final Map<UUID, RefreshSession> sessions = new ConcurrentHashMap<>();

            @Override
            public void save(RefreshSession refreshSession) {
                sessions.put(refreshSession.getId(), refreshSession);
            }

            @Override
            public Optional<RefreshSession> findById(UUID id) {
                return Optional.ofNullable(sessions.get(id));
            }
        };
    }

    @Bean
    public RefreshTokenRepository refreshTokenRepository() {
        return new RefreshTokenRepository() {
            private final Map<UUID, RefreshToken> tokensById = new ConcurrentHashMap<>();
            private final Map<String, UUID> tokenIdByHash = new ConcurrentHashMap<>();

            @Override
            public void save(RefreshToken refreshToken) {
                tokensById.put(refreshToken.getId(), refreshToken);
                tokenIdByHash.put(refreshToken.getTokenHash(), refreshToken.getId());
            }

            @Override
            public Optional<RefreshToken> findById(UUID id) {
                return Optional.ofNullable(tokensById.get(id));
            }

            @Override
            public Optional<RefreshToken> findByTokenHash(String tokenHash) {
                UUID id = tokenIdByHash.get(tokenHash);
                if (id == null) {
                    return Optional.empty();
                }
                return Optional.ofNullable(tokensById.get(id));
            }
        };
    }

}
