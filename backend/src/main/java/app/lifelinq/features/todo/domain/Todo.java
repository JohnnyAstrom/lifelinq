package app.lifelinq.features.todo.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public final class Todo {
    private final UUID id;
    private final UUID householdId;
    private final String text;
    private TodoStatus status;
    private final LocalDate dueDate;
    private final LocalTime dueTime;

    public Todo(UUID id, UUID householdId, String text) {
        this(id, householdId, text, null, null);
    }

    public Todo(UUID id, UUID householdId, String text, LocalDate dueDate, LocalTime dueTime) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }
        if (dueTime != null && dueDate == null) {
            throw new IllegalArgumentException("dueDate must not be null when dueTime is set");
        }
        this.id = id;
        this.householdId = householdId;
        this.text = text;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.status = TodoStatus.OPEN;
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

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalTime getDueTime() {
        return dueTime;
    }

    public TodoStatus getStatus() {
        return status;
    }

    public void toggle(Instant now) {
        if (now == null) {
            throw new IllegalArgumentException("now must not be null");
        }
        if (status == TodoStatus.OPEN) {
            status = TodoStatus.COMPLETED;
        } else {
            status = TodoStatus.OPEN;
        }
    }
}
