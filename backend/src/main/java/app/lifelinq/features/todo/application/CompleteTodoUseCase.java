package app.lifelinq.features.todo.application;

import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoRepository;
import java.time.Instant;

final class CompleteTodoUseCase {
    private final TodoRepository todoRepository;

    public CompleteTodoUseCase(TodoRepository todoRepository) {
        if (todoRepository == null) {
            throw new IllegalArgumentException("todoRepository must not be null");
        }
        this.todoRepository = todoRepository;
    }

    public CompleteTodoResult execute(CompleteTodoCommand command) {
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

        Todo todo = todoRepository.findById(command.getTodoId()).orElse(null);
        if (todo == null) {
            return new CompleteTodoResult(false);
        }

        boolean completed = todo.complete(now);
        if (completed) {
            todoRepository.save(todo);
        }

        return new CompleteTodoResult(completed);
    }
}
