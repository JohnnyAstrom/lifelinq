package app.lifelinq.features.todo.api;

import java.util.UUID;

public final class CreateTodoRequest {
    private UUID householdId;
    private String text;

    public UUID getHouseholdId() {
        return householdId;
    }

    public void setHouseholdId(UUID householdId) {
        this.householdId = householdId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
