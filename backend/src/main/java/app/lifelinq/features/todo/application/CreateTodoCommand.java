package app.lifelinq.features.todo.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public final class CreateTodoCommand {
    private final UUID householdId;
    private final String text;
    private final LocalDate dueDate;
    private final LocalTime dueTime;

    public CreateTodoCommand(UUID householdId, String text) {
        this(householdId, text, null, null);
    }

    public CreateTodoCommand(UUID householdId, String text, LocalDate dueDate, LocalTime dueTime) {
        this.householdId = householdId;
        this.text = text;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
    }

    public UUID getHouseholdId() {
        return householdId;
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
