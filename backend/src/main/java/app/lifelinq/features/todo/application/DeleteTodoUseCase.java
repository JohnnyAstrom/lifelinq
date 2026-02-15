package app.lifelinq.features.todo.application;

import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoRepository;
import java.time.Instant;
import java.util.Optional;

final class DeleteTodoUseCase {
    private final TodoRepository todoRepository;

    public DeleteTodoUseCase(TodoRepository todoRepository) {
        if (todoRepository == null) {
            throw new IllegalArgumentException("todoRepository must not be null");
        }
        this.todoRepository = todoRepository;
    }

    public DeleteTodoResult execute(DeleteTodoCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.getTodoId() == null) {
            throw new IllegalArgumentException("todoId must not be null");
        }
        Instant now = command.getNow();
        if (now == null) {
            throw new IllegalArgumentException("now must not be null");
        }
        Optional<Todo> existing = todoRepository.findById(command.getTodoId());
        if (existing.isEmpty()) {
            return new DeleteTodoResult(false);
        }
        Todo todo = existing.get();
        if (todo.isDeleted()) {
            return new DeleteTodoResult(false);
        }
        todo.delete(now);
        todoRepository.save(todo);
        return new DeleteTodoResult(true);
    }
}
