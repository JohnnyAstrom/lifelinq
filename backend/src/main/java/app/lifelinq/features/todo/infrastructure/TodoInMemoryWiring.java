package app.lifelinq.features.todo.infrastructure;

import app.lifelinq.features.todo.application.TodoApplicationService;
import app.lifelinq.features.user.application.EnsureUserExistsUseCase;
import app.lifelinq.features.user.domain.User;
import app.lifelinq.features.user.domain.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class TodoInMemoryWiring {
    private TodoInMemoryWiring() {
    }

    public static TodoApplicationService createUseCases() {
        InMemoryTodoRepository repository = new InMemoryTodoRepository();
        EnsureUserExistsUseCase ensureUserExistsUseCase = new EnsureUserExistsUseCase(new InMemoryUserRepository());
        return new TodoApplicationService(repository, ensureUserExistsUseCase);
    }

    private static final class InMemoryUserRepository implements UserRepository {
        private final Map<UUID, User> storage = new HashMap<>();

        @Override
        public Optional<User> findById(UUID id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public void save(User user) {
            storage.put(user.getId(), user);
        }
    }
}
