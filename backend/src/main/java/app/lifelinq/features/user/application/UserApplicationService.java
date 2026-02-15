package app.lifelinq.features.user.application;

import java.util.UUID;

public class UserApplicationService {
    private final EnsureUserExistsUseCase ensureUserExistsUseCase;

    public UserApplicationService(EnsureUserExistsUseCase ensureUserExistsUseCase) {
        if (ensureUserExistsUseCase == null) {
            throw new IllegalArgumentException("ensureUserExistsUseCase must not be null");
        }
        this.ensureUserExistsUseCase = ensureUserExistsUseCase;
    }

    public void ensureUserExists(UUID userId) {
        ensureUserExistsUseCase.execute(userId);
    }
}
