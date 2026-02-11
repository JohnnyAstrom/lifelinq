package app.lifelinq.features.todo.api;

import java.util.UUID;

public final class CreateTodoResponse {
    private final UUID todoId;

    public CreateTodoResponse(UUID todoId) {
        this.todoId = todoId;
    }

    public UUID getTodoId() {
        return todoId;
    }
}
