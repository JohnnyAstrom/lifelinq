package app.lifelinq.features.todo.application;

import app.lifelinq.features.todo.domain.TodoScope;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public final class UpdateTodoCommand {
    private final UUID todoId;
    private final String text;
    private final TodoScope scope;
    private final LocalDate dueDate;
    private final LocalTime dueTime;
    private final Integer scopeYear;
    private final Integer scopeWeek;
    private final Integer scopeMonth;

    public UpdateTodoCommand(UUID todoId, String text, LocalDate dueDate, LocalTime dueTime) {
        this(todoId, text, dueDate != null ? TodoScope.DAY : TodoScope.LATER, dueDate, dueTime, null, null, null);
    }

    public UpdateTodoCommand(
            UUID todoId,
            String text,
            TodoScope scope,
            LocalDate dueDate,
            LocalTime dueTime,
            Integer scopeYear,
            Integer scopeWeek,
            Integer scopeMonth
    ) {
        if (todoId == null) {
            throw new IllegalArgumentException("todoId must not be null");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }
        this.todoId = todoId;
        this.text = text;
        this.scope = scope;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.scopeYear = scopeYear;
        this.scopeWeek = scopeWeek;
        this.scopeMonth = scopeMonth;
    }

    public UUID getTodoId() {
        return todoId;
    }

    public String getText() {
        return text;
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
}
