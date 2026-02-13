package app.lifelinq.features.todo.api;

import app.lifelinq.features.todo.domain.TodoStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public final class TodoItemResponse {
    private final UUID id;
    private final UUID householdId;
    private final String text;
    private final TodoStatus status;
    private final LocalDate dueDate;
    private final LocalTime dueTime;

    public TodoItemResponse(
            UUID id,
            UUID householdId,
            String text,
            TodoStatus status,
            LocalDate dueDate,
            LocalTime dueTime
    ) {
        this.id = id;
        this.householdId = householdId;
        this.text = text;
        this.status = status;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
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

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalTime getDueTime() {
        return dueTime;
    }
}
