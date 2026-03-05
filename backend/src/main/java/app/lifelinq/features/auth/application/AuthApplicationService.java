package app.lifelinq.features.auth.application;

import app.lifelinq.config.JwtSigner;
import app.lifelinq.features.auth.contract.UserContextView;
import app.lifelinq.features.auth.contract.UserMembershipView;
import app.lifelinq.features.auth.domain.AuthIdentityRepository;
import app.lifelinq.features.auth.domain.AuthProvider;
import app.lifelinq.features.auth.domain.AuthMailSender;
import app.lifelinq.features.auth.domain.MagicLinkChallengeRepository;
import app.lifelinq.features.auth.domain.MagicLinkTokenGenerator;
import app.lifelinq.features.auth.domain.RefreshSessionRepository;
import app.lifelinq.features.auth.domain.RefreshTokenGenerator;
import app.lifelinq.features.auth.domain.RefreshTokenHasher;
import app.lifelinq.features.auth.domain.RefreshTokenRepository;
import app.lifelinq.features.group.contract.UserDefaultGroupProvisioning;
import app.lifelinq.features.group.contract.UserGroupMembershipLookup;
import app.lifelinq.features.user.contract.UserAccountDeletion;
import app.lifelinq.features.user.contract.UserActiveGroupRead;
import app.lifelinq.features.user.contract.UserActiveGroupSelection;
import app.lifelinq.features.user.contract.UserProfileRead;
import app.lifelinq.features.user.contract.UserProvisioning;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class AuthApplicationService {
    private final UserProvisioning userProvisioning;
    private final UserAccountDeletion userAccountDeletion;
    private final UserActiveGroupSelection userActiveGroupSelection;
    private final UserActiveGroupRead userActiveGroupRead;
    private final UserProfileRead userProfileRead;
    private final UserDefaultGroupProvisioning userDefaultGroupProvisioning;
    private final UserGroupMembershipLookup userGroupMembershipLookup;
    private final ResolveUserIdentityUseCase resolveUserIdentityUseCase;
    private final StartMagicLinkLoginUseCase startMagicLinkLoginUseCase;
    private final VerifyMagicLinkUseCase verifyMagicLinkUseCase;
    private final IssueRefreshSessionUseCase issueRefreshSessionUseCase;
    private final RotateRefreshTokenUseCase rotateRefreshTokenUseCase;
    private final RevokeRefreshSessionUseCase revokeRefreshSessionUseCase;
    private final JwtSigner jwtSigner;
    private final Clock clock;
    private final Duration magicLinkTtl;
    private final Duration maxMagicLinkTtl;
    private final Duration refreshIdleTtl;
    private final Duration refreshAbsoluteTtl;
    private final String magicLinkVerifyBaseUrl;
    private final String magicLinkCompleteBaseUrl;

    public AuthApplicationService(
            UserProvisioning userProvisioning,
            UserAccountDeletion userAccountDeletion,
            UserActiveGroupSelection userActiveGroupSelection,
            UserActiveGroupRead userActiveGroupRead,
            UserProfileRead userProfileRead,
            UserDefaultGroupProvisioning userDefaultGroupProvisioning,
            UserGroupMembershipLookup userGroupMembershipLookup,
            AuthIdentityRepository authIdentityRepository,
            MagicLinkChallengeRepository magicLinkChallengeRepository,
            MagicLinkTokenGenerator magicLinkTokenGenerator,
            AuthMailSender authMailSender,
            RefreshSessionRepository refreshSessionRepository,
            RefreshTokenRepository refreshTokenRepository,
            RefreshTokenGenerator refreshTokenGenerator,
            RefreshTokenHasher refreshTokenHasher,
            JwtSigner jwtSigner,
            Clock clock,
            Duration magicLinkTtl,
            Duration maxMagicLinkTtl,
            Duration refreshIdleTtl,
            Duration refreshAbsoluteTtl,
            String magicLinkVerifyBaseUrl,
            String magicLinkCompleteBaseUrl
    ) {
        if (userProvisioning == null) {
            throw new IllegalArgumentException("userProvisioning must not be null");
        }
        if (userAccountDeletion == null) {
            throw new IllegalArgumentException("userAccountDeletion must not be null");
        }
        if (userActiveGroupSelection == null) {
            throw new IllegalArgumentException("userActiveGroupSelection must not be null");
        }
        if (userActiveGroupRead == null) {
            throw new IllegalArgumentException("userActiveGroupRead must not be null");
        }
        if (userProfileRead == null) {
            throw new IllegalArgumentException("userProfileRead must not be null");
        }
        if (userDefaultGroupProvisioning == null) {
            throw new IllegalArgumentException("userDefaultGroupProvisioning must not be null");
        }
        if (userGroupMembershipLookup == null) {
            throw new IllegalArgumentException("userGroupMembershipLookup must not be null");
        }
        if (jwtSigner == null) {
            throw new IllegalArgumentException("jwtSigner must not be null");
        }
        if (authIdentityRepository == null) {
            throw new IllegalArgumentException("authIdentityRepository must not be null");
        }
        if (magicLinkChallengeRepository == null) {
            throw new IllegalArgumentException("magicLinkChallengeRepository must not be null");
        }
        if (magicLinkTokenGenerator == null) {
            throw new IllegalArgumentException("magicLinkTokenGenerator must not be null");
        }
        if (authMailSender == null) {
            throw new IllegalArgumentException("authMailSender must not be null");
        }
        if (refreshSessionRepository == null) {
            throw new IllegalArgumentException("refreshSessionRepository must not be null");
        }
        if (refreshTokenRepository == null) {
            throw new IllegalArgumentException("refreshTokenRepository must not be null");
        }
        if (refreshTokenGenerator == null) {
            throw new IllegalArgumentException("refreshTokenGenerator must not be null");
        }
        if (refreshTokenHasher == null) {
            throw new IllegalArgumentException("refreshTokenHasher must not be null");
        }
        if (clock == null) {
            throw new IllegalArgumentException("clock must not be null");
        }
        if (magicLinkTtl == null || magicLinkTtl.isZero() || magicLinkTtl.isNegative()) {
            throw new IllegalArgumentException("magicLinkTtl must be positive");
        }
        if (maxMagicLinkTtl == null || maxMagicLinkTtl.isZero() || maxMagicLinkTtl.isNegative()) {
            throw new IllegalArgumentException("maxMagicLinkTtl must be positive");
        }
        if (magicLinkTtl.compareTo(maxMagicLinkTtl) > 0) {
            throw new IllegalArgumentException("magicLinkTtl must not exceed maxMagicLinkTtl");
        }
        if (refreshIdleTtl == null || refreshIdleTtl.isZero() || refreshIdleTtl.isNegative()) {
            throw new IllegalArgumentException("refreshIdleTtl must be positive");
        }
        if (refreshAbsoluteTtl == null || refreshAbsoluteTtl.isZero() || refreshAbsoluteTtl.isNegative()) {
            throw new IllegalArgumentException("refreshAbsoluteTtl must be positive");
        }
        if (refreshIdleTtl.compareTo(refreshAbsoluteTtl) > 0) {
            throw new IllegalArgumentException("refreshIdleTtl must not exceed refreshAbsoluteTtl");
        }
        if (magicLinkVerifyBaseUrl == null || magicLinkVerifyBaseUrl.isBlank()) {
            throw new IllegalArgumentException("magicLinkVerifyBaseUrl must not be blank");
        }
        if (magicLinkCompleteBaseUrl == null || magicLinkCompleteBaseUrl.isBlank()) {
            throw new IllegalArgumentException("magicLinkCompleteBaseUrl must not be blank");
        }
        this.userProvisioning = userProvisioning;
        this.userAccountDeletion = userAccountDeletion;
        this.userActiveGroupSelection = userActiveGroupSelection;
        this.userActiveGroupRead = userActiveGroupRead;
        this.userProfileRead = userProfileRead;
        this.userDefaultGroupProvisioning = userDefaultGroupProvisioning;
        this.userGroupMembershipLookup = userGroupMembershipLookup;
        this.resolveUserIdentityUseCase = new ResolveUserIdentityUseCase(authIdentityRepository, userProvisioning);
        this.startMagicLinkLoginUseCase = new StartMagicLinkLoginUseCase(
                magicLinkChallengeRepository,
                magicLinkTokenGenerator,
                authMailSender
        );
        this.verifyMagicLinkUseCase = new VerifyMagicLinkUseCase(magicLinkChallengeRepository);
        this.issueRefreshSessionUseCase = new IssueRefreshSessionUseCase(
                refreshSessionRepository,
                refreshTokenRepository,
                refreshTokenGenerator,
                refreshTokenHasher
        );
        this.rotateRefreshTokenUseCase = new RotateRefreshTokenUseCase(
                refreshSessionRepository,
                refreshTokenRepository,
                refreshTokenGenerator,
                refreshTokenHasher
        );
        this.revokeRefreshSessionUseCase = new RevokeRefreshSessionUseCase(
                refreshSessionRepository,
                refreshTokenRepository,
                refreshTokenHasher
        );
        this.jwtSigner = jwtSigner;
        this.clock = clock;
        this.magicLinkTtl = magicLinkTtl;
        this.maxMagicLinkTtl = maxMagicLinkTtl;
        this.refreshIdleTtl = refreshIdleTtl;
        this.refreshAbsoluteTtl = refreshAbsoluteTtl;
        this.magicLinkVerifyBaseUrl = trimTrailingSlash(magicLinkVerifyBaseUrl);
        this.magicLinkCompleteBaseUrl = magicLinkCompleteBaseUrl;
    }

    public AuthTokenPair devLogin(String email) {
        return devLogin(email, null);
    }

    public AuthTokenPair devLogin(String email, String initialPlaceName) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
        ResolvedUserIdentity resolvedIdentity = resolveUserIdentityUseCase.execute(new ResolveUserIdentityCommand(
                ResolveUserIdentityCommand.LoginMethod.DEV,
                null,
                null,
                email,
                true
        ));
        return issueAuthPairForUser(resolvedIdentity.userId(), initialPlaceName, resolvedIdentity.normalizedEmail());
    }

    @Transactional
    public AuthTokenPair issueAuthPairForUser(UUID userId) {
        return issueAuthPairForUser(userId, null);
    }

    @Transactional
    public void startMagicLinkLogin(String email) {
        if (magicLinkTtl.compareTo(maxMagicLinkTtl) > 0) {
            throw new IllegalArgumentException("magic link ttl exceeds configured maximum");
        }
        startMagicLinkLoginUseCase.execute(new StartMagicLinkLoginCommand(
                email,
                clock.instant(),
                magicLinkTtl,
                magicLinkVerifyBaseUrl
        ));
    }

    @Transactional
    public String verifyMagicLinkAndBuildRedirect(String token) {
        VerifiedMagicLinkResult verified = verifyMagicLinkUseCase.execute(new VerifyMagicLinkCommand(token, clock.instant()));
        ResolvedUserIdentity resolvedIdentity = resolveUserIdentityUseCase.execute(new ResolveUserIdentityCommand(
                ResolveUserIdentityCommand.LoginMethod.MAGIC_LINK,
                null,
                null,
                verified.getNormalizedEmail(),
                true
        ));
        AuthTokenPair authTokenPair = issueAuthPairForUser(
                resolvedIdentity.userId(),
                null,
                resolvedIdentity.normalizedEmail()
        );
        String encodedAccessToken = URLEncoder.encode(authTokenPair.accessToken(), StandardCharsets.UTF_8);
        String encodedRefreshToken = URLEncoder.encode(authTokenPair.refreshToken(), StandardCharsets.UTF_8);
        return magicLinkCompleteBaseUrl + "#token=" + encodedAccessToken + "&refresh=" + encodedRefreshToken;
    }

    @Transactional
    public AuthTokenPair issueAuthPairForOAuthLogin(String provider, String subject, String email, boolean emailVerified) {
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("provider must not be blank");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("subject must not be blank");
        }
        AuthProvider oauthProvider = mapOAuthProvider(provider);
        ResolvedUserIdentity resolvedIdentity = resolveUserIdentityUseCase.execute(new ResolveUserIdentityCommand(
                ResolveUserIdentityCommand.LoginMethod.OAUTH,
                oauthProvider,
                subject,
                email,
                emailVerified
        ));
        return issueAuthPairForUser(resolvedIdentity.userId(), null, resolvedIdentity.normalizedEmail());
    }

    @Transactional
    public String ensureProvisionedAndSignToken(UUID userId) {
        return ensureProvisionedAndSignToken(userId, null);
    }

    @Transactional
    public String ensureProvisionedAndSignToken(UUID userId, String initialPlaceName) {
        return ensureProvisionedAndSignToken(userId, initialPlaceName, null);
    }

    private String ensureProvisionedAndSignToken(UUID userId, String initialPlaceName, String normalizedEmail) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        userProvisioning.ensureUserExists(userId, normalizedEmail);
        UUID groupId = userDefaultGroupProvisioning.ensureDefaultGroupProvisioned(userId, initialPlaceName);
        if (userActiveGroupRead.getActiveGroupId(userId) == null) {
            userActiveGroupSelection.setActiveGroup(userId, groupId);
        }
        return jwtSigner.sign(userId);
    }

    @Transactional
    public AuthTokenPair refreshAuthTokens(String refreshToken) {
        Instant now = clock.instant();
        RotateRefreshTokenResult rotated = rotateRefreshTokenUseCase.execute(
                refreshToken,
                now,
                refreshIdleTtl
        );
        return new AuthTokenPair(
                ensureProvisionedAndSignToken(rotated.userId()),
                rotated.refreshToken()
        );
    }

    @Transactional
    public void logoutRefreshSession(String refreshToken) {
        revokeRefreshSessionUseCase.execute(refreshToken, clock.instant());
    }

    public String signDevToken(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        return jwtSigner.sign(userId);
    }

    public void deleteAccount(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        userAccountDeletion.deleteAccount(userId);
    }

    public UserContextView getMe(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        return buildUserContext(userId);
    }

    public UserContextView setActiveGroup(UUID userId, UUID groupId) {
        if (userId == null || groupId == null) {
            throw new IllegalArgumentException("userId/groupId must not be null");
        }
        if (!userGroupMembershipLookup.isMember(userId, groupId)) {
            throw new ActiveGroupSelectionConflictException("Selected group is not a membership of the current user");
        }
        userActiveGroupSelection.setActiveGroup(userId, groupId);
        return buildUserContext(userId);
    }

    private UserContextView buildUserContext(UUID userId) {
        UUID activeGroupId = userActiveGroupRead.getActiveGroupId(userId);
        UUID defaultGroupId = defaultGroupIdFor(userId);
        var profile = userProfileRead.getProfile(userId);
        List<UserMembershipView> memberships = new ArrayList<>();
        for (var membership : userGroupMembershipLookup.listMemberships(userId)) {
            memberships.add(new UserMembershipView(
                    membership.groupId(),
                    membership.groupName(),
                    membership.role(),
                    membership.groupId().equals(defaultGroupId)
            ));
        }
        return new UserContextView(userId, activeGroupId, profile.firstName(), profile.lastName(), memberships);
    }

    private UUID defaultGroupIdFor(UUID userId) {
        return UUID.nameUUIDFromBytes(("personal-group:" + userId).getBytes(StandardCharsets.UTF_8));
    }

    private AuthProvider mapOAuthProvider(String provider) {
        String normalizedProvider = provider.trim().toLowerCase(Locale.ROOT);
        return switch (normalizedProvider) {
            case "google" -> AuthProvider.GOOGLE;
            case "apple" -> AuthProvider.APPLE;
            default -> throw new IllegalArgumentException("Unsupported OAuth provider: " + provider);
        };
    }

    private AuthTokenPair issueAuthPairForUser(UUID userId, String initialPlaceName) {
        return issueAuthPairForUser(userId, initialPlaceName, null);
    }

    private AuthTokenPair issueAuthPairForUser(UUID userId, String initialPlaceName, String normalizedEmail) {
        String accessToken = ensureProvisionedAndSignToken(userId, initialPlaceName, normalizedEmail);
        IssueRefreshSessionResult refresh = issueRefreshSessionUseCase.execute(
                userId,
                clock.instant(),
                refreshIdleTtl,
                refreshAbsoluteTtl
        );
        return new AuthTokenPair(accessToken, refresh.refreshToken());
    }

    private String trimTrailingSlash(String value) {
        return value.replaceAll("/+$", "");
    }
}
