package app.lifelinq.features.todo.domain;

import java.util.Optional;
import java.util.UUID;

public interface TodoRepository {
    Optional<Todo> findById(UUID id);

    void save(Todo todo);

    java.util.List<Todo> findAll();
}
