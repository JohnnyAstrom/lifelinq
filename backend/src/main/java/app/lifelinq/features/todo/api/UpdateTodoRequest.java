package app.lifelinq.features.todo.api;

import app.lifelinq.features.todo.domain.TodoScope;
import java.time.LocalDate;
import java.time.LocalTime;

public final class UpdateTodoRequest {
    private String text;
    private TodoScope scope;
    private LocalDate dueDate;
    private LocalTime dueTime;
    private Integer scopeYear;
    private Integer scopeWeek;
    private Integer scopeMonth;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalTime getDueTime() {
        return dueTime;
    }

    public void setDueTime(LocalTime dueTime) {
        this.dueTime = dueTime;
    }

    public TodoScope getScope() {
        return scope;
    }

    public void setScope(TodoScope scope) {
        this.scope = scope;
    }

    public Integer getScopeYear() {
        return scopeYear;
    }

    public void setScopeYear(Integer scopeYear) {
        this.scopeYear = scopeYear;
    }

    public Integer getScopeWeek() {
        return scopeWeek;
    }

    public void setScopeWeek(Integer scopeWeek) {
        this.scopeWeek = scopeWeek;
    }

    public Integer getScopeMonth() {
        return scopeMonth;
    }

    public void setScopeMonth(Integer scopeMonth) {
        this.scopeMonth = scopeMonth;
    }
}
