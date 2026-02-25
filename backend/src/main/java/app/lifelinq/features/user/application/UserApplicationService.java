package app.lifelinq.features.user.application;

import app.lifelinq.features.user.contract.UserProvisioning;
import java.util.UUID;

public class UserApplicationService implements UserProvisioning {
    private final EnsureUserExistsUseCase ensureUserExistsUseCase;

    public UserApplicationService(EnsureUserExistsUseCase ensureUserExistsUseCase) {
        if (ensureUserExistsUseCase == null) {
            throw new IllegalArgumentException("ensureUserExistsUseCase must not be null");
        }
        this.ensureUserExistsUseCase = ensureUserExistsUseCase;
    }

    @Override
    public void ensureUserExists(UUID userId) {
        ensureUserExistsUseCase.execute(userId);
    }
}
