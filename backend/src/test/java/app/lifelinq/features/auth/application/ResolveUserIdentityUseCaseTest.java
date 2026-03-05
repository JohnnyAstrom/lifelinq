package app.lifelinq.features.auth.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.auth.domain.AuthIdentity;
import app.lifelinq.features.auth.domain.AuthIdentityRepository;
import app.lifelinq.features.auth.domain.AuthProvider;
import app.lifelinq.features.user.contract.UserProvisioning;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ResolveUserIdentityUseCaseTest {

    @Test
    void oauthLoginWithExistingProviderIdentityResolvesExistingUser() {
        InMemoryAuthIdentityRepository identityRepository = new InMemoryAuthIdentityRepository();
        InMemoryUserProvisioning userProvisioning = new InMemoryUserProvisioning();
        UUID existingUserId = userProvisioning.provisionExistingUser("existing@example.com");
        identityRepository.save(new AuthIdentity(UUID.randomUUID(), AuthProvider.GOOGLE, "google-sub", existingUserId));
        ResolveUserIdentityUseCase useCase = new ResolveUserIdentityUseCase(identityRepository, userProvisioning);

        ResolvedUserIdentity result = useCase.execute(new ResolveUserIdentityCommand(
                ResolveUserIdentityCommand.LoginMethod.OAUTH,
                AuthProvider.GOOGLE,
                "google-sub",
                "existing@example.com",
                true
        ));

        assertEquals(existingUserId, result.userId());
        assertFalse(result.newUser());
        assertEquals(0, userProvisioning.createdUsersCount());
        assertEquals(existingUserId, identityRepository.findByProviderAndSubject(AuthProvider.GOOGLE, "google-sub")
                .orElseThrow().getUserId());
    }

    @Test
    void oauthLoginWithNewProviderIdentityCreatesUserAndLinksIdentity() {
        InMemoryAuthIdentityRepository identityRepository = new InMemoryAuthIdentityRepository();
        InMemoryUserProvisioning userProvisioning = new InMemoryUserProvisioning();
        ResolveUserIdentityUseCase useCase = new ResolveUserIdentityUseCase(identityRepository, userProvisioning);

        ResolvedUserIdentity result = useCase.execute(new ResolveUserIdentityCommand(
                ResolveUserIdentityCommand.LoginMethod.OAUTH,
                AuthProvider.GOOGLE,
                "new-google-sub",
                null,
                false
        ));

        assertNotNull(result.userId());
        assertTrue(result.newUser());
        assertEquals(1, userProvisioning.createdUsersCount());
        assertEquals(1, userProvisioning.userCount());
        assertEquals(result.userId(), identityRepository.findByProviderAndSubject(AuthProvider.GOOGLE, "new-google-sub")
                .orElseThrow().getUserId());
    }

    @Test
    void oauthLoginWithVerifiedEmailLinksToExistingUser() {
        InMemoryAuthIdentityRepository identityRepository = new InMemoryAuthIdentityRepository();
        InMemoryUserProvisioning userProvisioning = new InMemoryUserProvisioning();
        UUID existingUserId = userProvisioning.provisionExistingUser("user@example.com");
        identityRepository.save(new AuthIdentity(UUID.randomUUID(), AuthProvider.EMAIL, "user@example.com", existingUserId));
        ResolveUserIdentityUseCase useCase = new ResolveUserIdentityUseCase(identityRepository, userProvisioning);

        ResolvedUserIdentity result = useCase.execute(new ResolveUserIdentityCommand(
                ResolveUserIdentityCommand.LoginMethod.OAUTH,
                AuthProvider.GOOGLE,
                "verified-sub",
                " User@Example.com ",
                true
        ));

        assertEquals(existingUserId, result.userId());
        assertFalse(result.newUser());
        assertEquals(0, userProvisioning.createdUsersCount());
        assertEquals(1, userProvisioning.userCount());
        assertEquals(existingUserId, identityRepository.findByProviderAndSubject(AuthProvider.GOOGLE, "verified-sub")
                .orElseThrow().getUserId());
    }

    @Test
    void magicLinkLoginForExistingUserResolvesExistingUser() {
        InMemoryAuthIdentityRepository identityRepository = new InMemoryAuthIdentityRepository();
        InMemoryUserProvisioning userProvisioning = new InMemoryUserProvisioning();
        UUID existingUserId = userProvisioning.provisionExistingUser("magic@example.com");
        identityRepository.save(new AuthIdentity(UUID.randomUUID(), AuthProvider.EMAIL, "magic@example.com", existingUserId));
        ResolveUserIdentityUseCase useCase = new ResolveUserIdentityUseCase(identityRepository, userProvisioning);

        ResolvedUserIdentity result = useCase.execute(new ResolveUserIdentityCommand(
                ResolveUserIdentityCommand.LoginMethod.MAGIC_LINK,
                null,
                null,
                "magic@example.com",
                true
        ));

        assertEquals(existingUserId, result.userId());
        assertFalse(result.newUser());
        assertEquals(0, userProvisioning.createdUsersCount());
        assertEquals(1, userProvisioning.userCount());
    }

    @Test
    void magicLinkLoginForNewUserCreatesUserAndEmailIdentity() {
        InMemoryAuthIdentityRepository identityRepository = new InMemoryAuthIdentityRepository();
        InMemoryUserProvisioning userProvisioning = new InMemoryUserProvisioning();
        ResolveUserIdentityUseCase useCase = new ResolveUserIdentityUseCase(identityRepository, userProvisioning);

        ResolvedUserIdentity result = useCase.execute(new ResolveUserIdentityCommand(
                ResolveUserIdentityCommand.LoginMethod.MAGIC_LINK,
                null,
                null,
                "new-magic@example.com",
                true
        ));

        assertTrue(result.newUser());
        assertEquals(1, userProvisioning.createdUsersCount());
        assertEquals(1, userProvisioning.userCount());
        assertEquals(result.userId(), identityRepository.findByProviderAndSubject(AuthProvider.EMAIL, "new-magic@example.com")
                .orElseThrow().getUserId());
    }

    @Test
    void devLoginResolvesExistingUserByEmailWithoutDuplicates() {
        InMemoryAuthIdentityRepository identityRepository = new InMemoryAuthIdentityRepository();
        InMemoryUserProvisioning userProvisioning = new InMemoryUserProvisioning();
        UUID existingUserId = userProvisioning.provisionExistingUser("dev@example.com");
        ResolveUserIdentityUseCase useCase = new ResolveUserIdentityUseCase(identityRepository, userProvisioning);

        ResolvedUserIdentity result = useCase.execute(new ResolveUserIdentityCommand(
                ResolveUserIdentityCommand.LoginMethod.DEV,
                null,
                null,
                " Dev@example.com ",
                true
        ));

        assertEquals(existingUserId, result.userId());
        assertFalse(result.newUser());
        assertEquals(0, userProvisioning.createdUsersCount());
        assertEquals(1, userProvisioning.userCount());
        assertEquals(existingUserId, identityRepository.findByProviderAndSubject(AuthProvider.EMAIL, "dev@example.com")
                .orElseThrow().getUserId());
    }

    private static final class InMemoryAuthIdentityRepository implements AuthIdentityRepository {
        private final Map<String, AuthIdentity> byProviderAndSubject = new HashMap<>();

        @Override
        public Optional<AuthIdentity> findByProviderAndSubject(AuthProvider provider, String subject) {
            return Optional.ofNullable(byProviderAndSubject.get(provider.name() + ":" + subject));
        }

        @Override
        public void save(AuthIdentity identity) {
            byProviderAndSubject.put(identity.getProvider().name() + ":" + identity.getSubject(), identity);
        }
    }

    private static final class InMemoryUserProvisioning implements UserProvisioning {
        private final Map<UUID, String> usersById = new HashMap<>();
        private final Map<String, UUID> userIdByEmail = new HashMap<>();
        private int createdUsersCount = 0;

        @Override
        public UUID ensureUserExistsAndResolveUserId(UUID proposedUserId, String email) {
            if (proposedUserId == null) {
                throw new IllegalArgumentException("proposedUserId must not be null");
            }
            String normalizedEmail = normalizeEmailOrNull(email);

            if (normalizedEmail != null) {
                UUID existingByEmail = userIdByEmail.get(normalizedEmail);
                if (existingByEmail != null) {
                    return existingByEmail;
                }
            }

            if (usersById.containsKey(proposedUserId)) {
                if (normalizedEmail != null) {
                    usersById.put(proposedUserId, normalizedEmail);
                    userIdByEmail.put(normalizedEmail, proposedUserId);
                }
                return proposedUserId;
            }

            usersById.put(proposedUserId, normalizedEmail);
            if (normalizedEmail != null) {
                userIdByEmail.put(normalizedEmail, proposedUserId);
            }
            createdUsersCount++;
            return proposedUserId;
        }

        UUID provisionExistingUser(String email) {
            UUID userId = UUID.randomUUID();
            String normalized = normalizeEmailOrNull(email);
            usersById.put(userId, normalized);
            if (normalized != null) {
                userIdByEmail.put(normalized, userId);
            }
            return userId;
        }

        int createdUsersCount() {
            return createdUsersCount;
        }

        int userCount() {
            return usersById.size();
        }

        private String normalizeEmailOrNull(String value) {
            if (value == null) {
                return null;
            }
            String normalized = value.trim().toLowerCase(Locale.ROOT);
            return normalized.isBlank() ? null : normalized;
        }
    }
}

