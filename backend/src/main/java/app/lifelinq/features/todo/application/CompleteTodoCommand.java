package app.lifelinq.features.todo.application;

import java.time.Instant;
import java.util.UUID;

public final class CompleteTodoCommand {
    private final UUID todoId;
    private final Instant now;

    public CompleteTodoCommand(UUID todoId, Instant now) {
        this.todoId = todoId;
        this.now = now;
    }

    public UUID getTodoId() {
        return todoId;
    }

    public Instant getNow() {
        return now;
    }
}
