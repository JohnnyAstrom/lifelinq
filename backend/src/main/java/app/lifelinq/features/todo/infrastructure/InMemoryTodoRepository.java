package app.lifelinq.features.todo.infrastructure;

import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class InMemoryTodoRepository implements TodoRepository {
    private final List<Todo> todos = new ArrayList<>();

    @Override
    public Optional<Todo> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        for (Todo todo : todos) {
            if (id.equals(todo.getId())) {
                return Optional.of(todo);
            }
        }
        return Optional.empty();
    }

    @Override
    public void save(Todo todo) {
        if (todo == null) {
            throw new IllegalArgumentException("todo must not be null");
        }
        todos.add(todo);
    }

    @Override
    public List<Todo> findAll() {
        return new ArrayList<>(todos);
    }
}
