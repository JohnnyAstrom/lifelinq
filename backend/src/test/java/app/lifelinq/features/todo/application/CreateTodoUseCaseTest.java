package app.lifelinq.features.todo.application;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class CreateTodoUseCaseTest {

    @Test
    void createsTodoAndReturnsId() {
        CreateTodoUseCase useCase = new CreateTodoUseCase();
        CreateTodoCommand command = new CreateTodoCommand(UUID.randomUUID(), "Buy milk");

        CreateTodoResult result = useCase.execute(command);

        assertNotNull(result);
        assertNotNull(result.getTodoId());
    }

    @Test
    void requiresCommand() {
        CreateTodoUseCase useCase = new CreateTodoUseCase();
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    @Test
    void requiresHouseholdId() {
        CreateTodoUseCase useCase = new CreateTodoUseCase();
        CreateTodoCommand command = new CreateTodoCommand(null, "Buy milk");
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }

    @Test
    void requiresNonBlankText() {
        CreateTodoUseCase useCase = new CreateTodoUseCase();
        CreateTodoCommand command = new CreateTodoCommand(UUID.randomUUID(), " ");
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }
}
