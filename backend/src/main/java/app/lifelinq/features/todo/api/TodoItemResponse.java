package app.lifelinq.features.todo.api;

import app.lifelinq.features.todo.domain.TodoStatus;
import java.util.UUID;

public final class TodoItemResponse {
    private final UUID id;
    private final UUID householdId;
    private final String text;
    private final TodoStatus status;

    public TodoItemResponse(UUID id, UUID householdId, String text, TodoStatus status) {
        this.id = id;
        this.householdId = householdId;
        this.text = text;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public UUID getHouseholdId() {
        return householdId;
    }

    public String getText() {
        return text;
    }

    public TodoStatus getStatus() {
        return status;
    }
}
