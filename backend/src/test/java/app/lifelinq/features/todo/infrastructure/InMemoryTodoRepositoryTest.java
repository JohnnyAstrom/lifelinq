package app.lifelinq.features.todo.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.lifelinq.features.todo.domain.Todo;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InMemoryTodoRepositoryTest {

    @Test
    void requiresTodo() {
        InMemoryTodoRepository repository = new InMemoryTodoRepository();
        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
    }

    @Test
    void findsById() {
        InMemoryTodoRepository repository = new InMemoryTodoRepository();
        Todo todo = new Todo(UUID.randomUUID(), UUID.randomUUID(), "Task");
        repository.save(todo);

        assertEquals(true, repository.findById(todo.getId()).isPresent());
    }
}
