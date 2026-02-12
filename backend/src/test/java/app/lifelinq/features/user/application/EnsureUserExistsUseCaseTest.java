package app.lifelinq.features.user.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.user.domain.User;
import app.lifelinq.features.user.domain.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EnsureUserExistsUseCaseTest {

    @Test
    void createsUserWhenMissing() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        EnsureUserExistsUseCase useCase = new EnsureUserExistsUseCase(repository);
        UUID userId = UUID.randomUUID();

        useCase.execute(userId);

        Optional<User> found = repository.findById(userId);
        assertTrue(found.isPresent());
        assertEquals(userId, found.get().getId());
        assertEquals(1, repository.saveCount);
    }

    @Test
    void doesNotCreateUserWhenExists() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        UUID userId = UUID.randomUUID();
        repository.save(new User(userId));
        EnsureUserExistsUseCase useCase = new EnsureUserExistsUseCase(repository);

        useCase.execute(userId);

        assertEquals(1, repository.saveCount);
    }

    private static final class InMemoryUserRepository implements UserRepository {
        private final Map<UUID, User> storage = new HashMap<>();
        private int saveCount = 0;

        @Override
        public Optional<User> findById(UUID id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public void save(User user) {
            storage.put(user.getId(), user);
            saveCount++;
        }
    }
}
