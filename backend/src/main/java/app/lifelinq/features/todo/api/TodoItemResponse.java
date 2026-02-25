package app.lifelinq.features.todo.api;

import app.lifelinq.features.todo.domain.TodoScope;
import app.lifelinq.features.todo.domain.TodoStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public final class TodoItemResponse {
    private final UUID id;
    private final UUID groupId;
    private final String text;
    private final TodoStatus status;
    private final TodoScope scope;
    private final LocalDate dueDate;
    private final LocalTime dueTime;
    private final Integer scopeYear;
    private final Integer scopeWeek;
    private final Integer scopeMonth;
    private final Instant completedAt;
    private final Instant createdAt;

    public TodoItemResponse(
            UUID id,
            UUID groupId,
            String text,
            TodoStatus status,
            TodoScope scope,
            LocalDate dueDate,
            LocalTime dueTime,
            Integer scopeYear,
            Integer scopeWeek,
            Integer scopeMonth,
            Instant completedAt,
            Instant createdAt
    ) {
        this.id = id;
        this.groupId = groupId;
        this.text = text;
        this.status = status;
        this.scope = scope;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.scopeYear = scopeYear;
        this.scopeWeek = scopeWeek;
        this.scopeMonth = scopeMonth;
        this.completedAt = completedAt;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public String getText() {
        return text;
    }

    public TodoStatus getStatus() {
        return status;
    }

    public TodoScope getScope() {
        return scope;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalTime getDueTime() {
        return dueTime;
    }

    public Integer getScopeYear() {
        return scopeYear;
    }

    public Integer getScopeWeek() {
        return scopeWeek;
    }

    public Integer getScopeMonth() {
        return scopeMonth;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
