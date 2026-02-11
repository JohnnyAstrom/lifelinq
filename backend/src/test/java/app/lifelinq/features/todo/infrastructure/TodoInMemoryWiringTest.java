package app.lifelinq.features.todo.infrastructure;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import app.lifelinq.features.todo.application.TodoUseCases;
import org.junit.jupiter.api.Test;

class TodoInMemoryWiringTest {

    @Test
    void createsAllUseCases() {
        TodoUseCases useCases = TodoInMemoryWiring.createUseCases();

        assertNotNull(useCases);
        assertNotNull(useCases.createTodo());
        assertNotNull(useCases.completeTodo());
        assertNotNull(useCases.listTodos());
    }
}
