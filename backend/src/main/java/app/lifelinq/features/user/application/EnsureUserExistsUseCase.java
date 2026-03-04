package app.lifelinq.features.user.application;

import app.lifelinq.features.user.domain.User;
import app.lifelinq.features.user.domain.UserRepository;
import java.util.Locale;
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
        execute(userId, null);
    }

    public void execute(UUID userId, String email) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        String normalizedEmail = normalizeEmailOrNull(email);
        var existing = userRepository.findById(userId);
        if (existing.isPresent()) {
            if (normalizedEmail != null && !normalizedEmail.equals(existing.get().getEmail())) {
                userRepository.save(existing.get().withEmail(normalizedEmail));
            }
            return;
        }
        userRepository.save(new User(userId, null, normalizedEmail, null, null));
    }

    private String normalizeEmailOrNull(String email) {
        if (email == null) {
            return null;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        return normalized.isBlank() ? null : normalized;
    }
}
