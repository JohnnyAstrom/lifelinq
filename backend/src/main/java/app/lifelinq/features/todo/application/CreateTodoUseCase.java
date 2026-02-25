package app.lifelinq.features.todo.application;

import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoRepository;
import java.time.Instant;
import java.util.UUID;

final class CreateTodoUseCase {
    private final TodoRepository todoRepository;

    public CreateTodoUseCase(TodoRepository todoRepository) {
        if (todoRepository == null) {
            throw new IllegalArgumentException("todoRepository must not be null");
        }
        this.todoRepository = todoRepository;
    }

    public CreateTodoResult execute(CreateTodoCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.getGroupId() == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        if (command.getText() == null || command.getText().isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }

        Todo todo = new Todo(
                UUID.randomUUID(),
                command.getGroupId(),
                command.getText(),
                command.getScope(),
                command.getDueDate(),
                command.getDueTime(),
                command.getScopeYear(),
                command.getScopeWeek(),
                command.getScopeMonth(),
                Instant.now()
        );
        todoRepository.save(todo);
        return new CreateTodoResult(todo.getId());
    }
}
