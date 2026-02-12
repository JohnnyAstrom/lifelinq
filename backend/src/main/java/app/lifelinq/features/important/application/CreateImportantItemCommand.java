package app.lifelinq.features.important.application;

import java.util.UUID;

public final class CreateImportantItemCommand {
    private final UUID householdId;
    private final String text;

    public CreateImportantItemCommand(UUID householdId, String text) {
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
