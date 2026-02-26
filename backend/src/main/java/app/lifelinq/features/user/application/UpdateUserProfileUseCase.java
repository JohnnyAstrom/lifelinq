package app.lifelinq.features.user.application;

import app.lifelinq.features.user.contract.InvalidUserProfileException;
import app.lifelinq.features.user.domain.UserRepository;
import java.util.UUID;

final class UpdateUserProfileUseCase {
    private final UserRepository userRepository;

    UpdateUserProfileUseCase(UserRepository userRepository) {
        if (userRepository == null) {
            throw new IllegalArgumentException("userRepository must not be null");
        }
        this.userRepository = userRepository;
    }

    void execute(UUID userId, String firstName, String lastName) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        if (firstName == null) {
            throw new InvalidUserProfileException("firstName must not be null");
        }
        if (lastName == null) {
            throw new InvalidUserProfileException("lastName must not be null");
        }
        String trimmedFirstName = firstName.trim();
        String trimmedLastName = lastName.trim();
        if (trimmedFirstName.isEmpty()) {
            throw new InvalidUserProfileException("firstName must not be blank");
        }
        if (trimmedLastName.isEmpty()) {
            throw new InvalidUserProfileException("lastName must not be blank");
        }

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        userRepository.save(user.withProfile(trimmedFirstName, trimmedLastName));
    }
}
