package app.lifelinq.features.todo.infrastructure;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import app.lifelinq.features.todo.application.TodoApplicationService;
import org.junit.jupiter.api.Test;

class TodoInMemoryWiringTest {

    @Test
    void createsApplicationService() {
        TodoApplicationService service = TodoInMemoryWiring.createUseCases();

        assertNotNull(service);
    }
}
