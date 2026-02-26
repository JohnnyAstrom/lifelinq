package app.lifelinq.features.user.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.lifelinq.features.user.contract.InvalidUserProfileException;
import app.lifelinq.features.user.domain.User;
import app.lifelinq.features.user.domain.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UpdateUserProfileUseCaseTest {

    @Test
    void updatesProfileSuccessfullyWithValidInput() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        UUID userId = UUID.randomUUID();
        repository.save(new User(userId));
        UpdateUserProfileUseCase useCase = new UpdateUserProfileUseCase(repository);

        useCase.execute(userId, "  Ada ", " Lovelace  ");

        User updated = repository.findById(userId).orElseThrow();
        assertEquals("Ada", updated.getFirstName());
        assertEquals("Lovelace", updated.getLastName());
    }

    @Test
    void throwsExceptionIfFirstNameIsNull() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        UUID userId = UUID.randomUUID();
        repository.save(new User(userId));
        UpdateUserProfileUseCase useCase = new UpdateUserProfileUseCase(repository);

        InvalidUserProfileException ex = assertThrows(
                InvalidUserProfileException.class,
                () -> useCase.execute(userId, null, "Lovelace")
        );

        assertEquals("firstName must not be null", ex.getMessage());
    }

    @Test
    void throwsExceptionIfLastNameIsNull() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        UUID userId = UUID.randomUUID();
        repository.save(new User(userId));
        UpdateUserProfileUseCase useCase = new UpdateUserProfileUseCase(repository);

        InvalidUserProfileException ex = assertThrows(
                InvalidUserProfileException.class,
                () -> useCase.execute(userId, "Ada", null)
        );

        assertEquals("lastName must not be null", ex.getMessage());
    }

    @Test
    void throwsExceptionIfFirstNameIsBlank() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        UUID userId = UUID.randomUUID();
        repository.save(new User(userId));
        UpdateUserProfileUseCase useCase = new UpdateUserProfileUseCase(repository);

        InvalidUserProfileException ex = assertThrows(
                InvalidUserProfileException.class,
                () -> useCase.execute(userId, "   ", "Lovelace")
        );

        assertEquals("firstName must not be blank", ex.getMessage());
    }

    @Test
    void throwsExceptionIfLastNameIsBlank() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        UUID userId = UUID.randomUUID();
        repository.save(new User(userId));
        UpdateUserProfileUseCase useCase = new UpdateUserProfileUseCase(repository);

        InvalidUserProfileException ex = assertThrows(
                InvalidUserProfileException.class,
                () -> useCase.execute(userId, "Ada", "   ")
        );

        assertEquals("lastName must not be blank", ex.getMessage());
    }

    @Test
    void ensuresRepositorySaveIsCalledWithUpdatedNames() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        UUID userId = UUID.randomUUID();
        repository.save(new User(userId, null, "Old", "Name"));
        UpdateUserProfileUseCase useCase = new UpdateUserProfileUseCase(repository);

        useCase.execute(userId, "New", "Profile");

        assertEquals(2, repository.saveCount); // initial seed + update
        assertNotNull(repository.lastSavedUser);
        assertEquals(userId, repository.lastSavedUser.getId());
        assertEquals("New", repository.lastSavedUser.getFirstName());
        assertEquals("Profile", repository.lastSavedUser.getLastName());
    }

    private static final class InMemoryUserRepository implements UserRepository {
        private final Map<UUID, User> storage = new HashMap<>();
        private int saveCount = 0;
        private User lastSavedUser;

        @Override
        public Optional<User> findById(UUID id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public void save(User user) {
            storage.put(user.getId(), user);
            saveCount++;
            lastSavedUser = user;
        }

        @Override
        public void deleteById(UUID id) {
            storage.remove(id);
        }
    }
}
