package app.lifelinq.features.todo.infrastructure;

import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryTodoRepository implements TodoRepository {
    private final ConcurrentMap<UUID, Todo> byId = new ConcurrentHashMap<>();

    @Override
    public Optional<Todo> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public void save(Todo todo) {
        if (todo == null) {
            throw new IllegalArgumentException("todo must not be null");
        }
        byId.put(todo.getId(), todo);
    }

    @Override
    public List<Todo> findAll() {
        return new ArrayList<>(byId.values());
    }
}
