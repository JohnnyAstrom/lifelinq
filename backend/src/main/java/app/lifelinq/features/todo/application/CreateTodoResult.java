package app.lifelinq.features.todo.application;

import java.util.UUID;

public final class CreateTodoResult {
    private final UUID todoId;

    public CreateTodoResult(UUID todoId) {
        this.todoId = todoId;
    }

    public UUID getTodoId() {
        return todoId;
    }
}
