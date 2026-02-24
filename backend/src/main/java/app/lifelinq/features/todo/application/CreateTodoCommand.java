package app.lifelinq.features.todo.application;

import app.lifelinq.features.todo.domain.TodoScope;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public final class CreateTodoCommand {
    private final UUID householdId;
    private final String text;
    private final TodoScope scope;
    private final LocalDate dueDate;
    private final LocalTime dueTime;
    private final Integer scopeYear;
    private final Integer scopeWeek;
    private final Integer scopeMonth;

    public CreateTodoCommand(UUID householdId, String text) {
        this(householdId, text, TodoScope.LATER, null, null, null, null, null);
    }

    public CreateTodoCommand(UUID householdId, String text, LocalDate dueDate, LocalTime dueTime) {
        this(householdId, text, dueDate != null ? TodoScope.DAY : TodoScope.LATER, dueDate, dueTime, null, null, null);
    }

    public CreateTodoCommand(
            UUID householdId,
            String text,
            TodoScope scope,
            LocalDate dueDate,
            LocalTime dueTime,
            Integer scopeYear,
            Integer scopeWeek,
            Integer scopeMonth
    ) {
        this.householdId = householdId;
        this.text = text;
        this.scope = scope;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.scopeYear = scopeYear;
        this.scopeWeek = scopeWeek;
        this.scopeMonth = scopeMonth;
    }

    public UUID getHouseholdId() {
        return householdId;
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
