package app.lifelinq.features.user.application;

import app.lifelinq.features.user.domain.UserRepository;

public final class UserApplicationServiceTestFactory {
    private UserApplicationServiceTestFactory() {
    }

    public static UserApplicationService create(UserRepository userRepository) {
        return new UserApplicationService(new EnsureUserExistsUseCase(userRepository));
    }
}
