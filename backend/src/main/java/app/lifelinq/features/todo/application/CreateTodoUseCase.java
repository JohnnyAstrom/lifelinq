package app.lifelinq.features.todo.application;

import app.lifelinq.features.todo.domain.Todo;
import java.util.UUID;

final class CreateTodoUseCase {

    public CreateTodoResult execute(CreateTodoCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.getHouseholdId() == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        if (command.getText() == null || command.getText().isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }

        Todo todo = new Todo(UUID.randomUUID(), command.getHouseholdId(), command.getText());
        return new CreateTodoResult(todo.getId());
    }
}
