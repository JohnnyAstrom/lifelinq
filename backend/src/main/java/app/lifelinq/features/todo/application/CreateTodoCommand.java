package app.lifelinq.features.todo.application;

import java.util.UUID;

public final class CreateTodoCommand {
    private final UUID householdId;
    private final String text;

    public CreateTodoCommand(UUID householdId, String text) {
        this.householdId = householdId;
        this.text = text;
    }

    public UUID getHouseholdId() {
        return householdId;
    }

    public String getText() {
        return text;
    }
}
