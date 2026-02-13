package app.lifelinq.features.todo.api;

import java.time.LocalDate;
import java.time.LocalTime;

public final class UpdateTodoRequest {
    private String text;
    private LocalDate dueDate;
    private LocalTime dueTime;

    public String getText() {
        return text;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalTime getDueTime() {
        return dueTime;
    }
}
