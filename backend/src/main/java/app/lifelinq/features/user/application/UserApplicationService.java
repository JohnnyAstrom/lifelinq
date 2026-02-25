package app.lifelinq.features.user.application;

import app.lifelinq.features.user.contract.UserProvisioning;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class UserApplicationService implements UserProvisioning {
    private final EnsureUserExistsUseCase ensureUserExistsUseCase;
    private final DeleteAccountUseCase deleteAccountUseCase;

    public UserApplicationService(
            EnsureUserExistsUseCase ensureUserExistsUseCase,
            DeleteAccountUseCase deleteAccountUseCase
    ) {
        if (ensureUserExistsUseCase == null) {
            throw new IllegalArgumentException("ensureUserExistsUseCase must not be null");
        }
        if (deleteAccountUseCase == null) {
            throw new IllegalArgumentException("deleteAccountUseCase must not be null");
        }
        this.ensureUserExistsUseCase = ensureUserExistsUseCase;
        this.deleteAccountUseCase = deleteAccountUseCase;
    }

    @Override
    public void ensureUserExists(UUID userId) {
        ensureUserExistsUseCase.execute(userId);
    }

    @Transactional
    public void deleteAccount(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        var memberships = deleteAccountUseCase.loadMemberships(userId);
        deleteAccountUseCase.validateGovernance(memberships);
        deleteAccountUseCase.deleteMemberships(userId);
        deleteAccountUseCase.deleteEmptyGroups(memberships);
        deleteAccountUseCase.deleteUser(userId);
    }
}
