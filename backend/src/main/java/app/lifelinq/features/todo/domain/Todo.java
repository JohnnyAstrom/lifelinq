package app.lifelinq.features.todo.domain;

import java.util.UUID;
import java.time.Instant;

public final class Todo {
    private final UUID id;
    private final UUID householdId;
    private final String text;
    private TodoStatus status;

    public Todo(UUID id, UUID householdId, String text) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }
        this.id = id;
        this.householdId = householdId;
        this.text = text;
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
