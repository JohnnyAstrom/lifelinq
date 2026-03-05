package app.lifelinq.features.user.domain;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findById(UUID id);

    default Optional<User> findByEmail(String email) {
        return Optional.empty();
    }

    void save(User user);

    void deleteById(UUID id);
}
