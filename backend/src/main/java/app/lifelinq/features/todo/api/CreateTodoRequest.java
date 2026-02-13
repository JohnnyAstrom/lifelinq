package app.lifelinq.features.todo.api;

import java.time.LocalDate;
import java.time.LocalTime;

public final class CreateTodoRequest {
    private String text;
    private LocalDate dueDate;
    private LocalTime dueTime;

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
}
