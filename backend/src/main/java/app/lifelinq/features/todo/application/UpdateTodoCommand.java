package app.lifelinq.features.todo.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public final class UpdateTodoCommand {
    private final UUID todoId;
    private final String text;
    private final LocalDate dueDate;
    private final LocalTime dueTime;

    public UpdateTodoCommand(UUID todoId, String text, LocalDate dueDate, LocalTime dueTime) {
        if (todoId == null) {
            throw new IllegalArgumentException("todoId must not be null");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }
        if (dueTime != null && dueDate == null) {
            throw new IllegalArgumentException("dueDate must not be null when dueTime is set");
        }
        this.todoId = todoId;
        this.text = text;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
    }

    public UUID getTodoId() {
        return todoId;
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
}
