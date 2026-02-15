package app.lifelinq.features.todo.application;

import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoRepository;
import app.lifelinq.features.todo.domain.TodoStatus;
import java.time.Instant;
import java.util.Optional;

final class UpdateTodoUseCase {
    private final TodoRepository todoRepository;

    public UpdateTodoUseCase(TodoRepository todoRepository) {
        if (todoRepository == null) {
            throw new IllegalArgumentException("todoRepository must not be null");
        }
        this.todoRepository = todoRepository;
    }

    public UpdateTodoResult execute(UpdateTodoCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        Optional<Todo> existing = todoRepository.findById(command.getTodoId());
        if (existing.isEmpty()) {
            return new UpdateTodoResult(false);
        }
        Todo current = existing.get();
        Todo updated = Todo.rehydrate(
                current.getId(),
                current.getHouseholdId(),
                command.getText(),
                current.getStatus(),
                command.getDueDate(),
                command.getDueTime(),
                current.getDeletedAt()
        );
        todoRepository.save(updated);
        return new UpdateTodoResult(true);
    }
}
