package app.lifelinq.features.auth.application;

import app.lifelinq.features.auth.domain.AuthIdentity;
import app.lifelinq.features.auth.domain.AuthIdentityRepository;
import app.lifelinq.features.auth.domain.AuthProvider;
import app.lifelinq.features.user.contract.UserProvisioning;
import java.util.Locale;
import java.util.UUID;

final class ResolveUserIdentityUseCase {
    private final AuthIdentityRepository authIdentityRepository;
    private final UserProvisioning userProvisioning;

    ResolveUserIdentityUseCase(AuthIdentityRepository authIdentityRepository, UserProvisioning userProvisioning) {
        if (authIdentityRepository == null) {
            throw new IllegalArgumentException("authIdentityRepository must not be null");
        }
        if (userProvisioning == null) {
            throw new IllegalArgumentException("userProvisioning must not be null");
        }
        this.authIdentityRepository = authIdentityRepository;
        this.userProvisioning = userProvisioning;
    }

    ResolvedUserIdentity execute(ResolveUserIdentityCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.loginMethod() == null) {
            throw new IllegalArgumentException("loginMethod must not be null");
        }
        String normalizedEmail = normalizeEmailOrNull(command.email());

        if (command.loginMethod() == ResolveUserIdentityCommand.LoginMethod.OAUTH) {
            validateOAuthInput(command.provider(), command.subject());
            var existingOauthIdentity = authIdentityRepository.findByProviderAndSubject(command.provider(), command.subject());
            if (existingOauthIdentity.isPresent()) {
                UUID resolvedUserId = userProvisioning.ensureUserExistsAndResolveUserId(existingOauthIdentity.get().getUserId(), null);
                return new ResolvedUserIdentity(resolvedUserId, normalizedEmail, false);
            }
        }

        boolean canLinkByEmail = normalizedEmail != null
                && (command.loginMethod() != ResolveUserIdentityCommand.LoginMethod.OAUTH || command.emailVerified());

        UUID userId = null;
        boolean newUser = false;

        if (canLinkByEmail) {
            var existingEmailIdentity = authIdentityRepository.findByProviderAndSubject(AuthProvider.EMAIL, normalizedEmail);
            if (existingEmailIdentity.isPresent()) {
                AuthIdentity identity = existingEmailIdentity.get();
                userId = userProvisioning.ensureUserExistsAndResolveUserId(identity.getUserId(), normalizedEmail);
                if (!userId.equals(identity.getUserId())) {
                    authIdentityRepository.save(new AuthIdentity(identity.getId(), AuthProvider.EMAIL, normalizedEmail, userId));
                }
            } else {
                UUID candidateUserId = UUID.randomUUID();
                userId = userProvisioning.ensureUserExistsAndResolveUserId(candidateUserId, normalizedEmail);
                newUser = userId.equals(candidateUserId);
                authIdentityRepository.save(new AuthIdentity(UUID.randomUUID(), AuthProvider.EMAIL, normalizedEmail, userId));
            }
        }

        if (userId == null) {
            UUID candidateUserId = UUID.randomUUID();
            userId = userProvisioning.ensureUserExistsAndResolveUserId(candidateUserId, normalizedEmail);
            newUser = userId.equals(candidateUserId);
        }

        if (command.loginMethod() == ResolveUserIdentityCommand.LoginMethod.OAUTH) {
            authIdentityRepository.save(new AuthIdentity(
                    UUID.randomUUID(),
                    command.provider(),
                    command.subject(),
                    userId
            ));
        }

        return new ResolvedUserIdentity(userId, normalizedEmail, newUser);
    }

    private void validateOAuthInput(AuthProvider provider, String subject) {
        if (provider == null) {
            throw new IllegalArgumentException("provider must not be null for oauth login");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("subject must not be blank for oauth login");
        }
    }

    private String normalizeEmailOrNull(String email) {
        if (email == null) {
            return null;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        return normalized.isBlank() ? null : normalized;
    }
}

