package app.lifelinq.features.todo.application;

import app.lifelinq.features.todo.domain.TodoStatus;
import java.util.UUID;

public final class TodoQuery {
    private final UUID groupId;
    private final TodoStatus statusFilter;

    public TodoQuery(UUID groupId, TodoStatus statusFilter) {
        this.groupId = groupId;
        this.statusFilter = statusFilter;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public TodoStatus getStatusFilter() {
        return statusFilter;
    }
}
