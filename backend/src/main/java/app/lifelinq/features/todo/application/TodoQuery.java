package app.lifelinq.features.todo.application;

import app.lifelinq.features.todo.domain.TodoStatus;
import java.util.UUID;

public final class TodoQuery {
    private final UUID householdId;
    private final TodoStatus statusFilter;

    public TodoQuery(UUID householdId, TodoStatus statusFilter) {
        this.householdId = householdId;
        this.statusFilter = statusFilter;
    }

    public UUID getHouseholdId() {
        return householdId;
    }

    public TodoStatus getStatusFilter() {
        return statusFilter;
    }
}
