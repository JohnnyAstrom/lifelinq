package app.lifelinq.features.user.application;

import app.lifelinq.features.user.domain.User;
import app.lifelinq.features.user.domain.UserRepository;
import java.util.UUID;

final class EnsureUserExistsUseCase {
    private final UserRepository userRepository;

    public EnsureUserExistsUseCase(UserRepository userRepository) {
        if (userRepository == null) {
            throw new IllegalArgumentException("userRepository must not be null");
        }
        this.userRepository = userRepository;
    }

    public void execute(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        if (userRepository.findById(userId).isPresent()) {
            return;
        }
        userRepository.save(new User(userId));
    }
}
