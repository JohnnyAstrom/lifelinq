package app.lifelinq.features.documents.application;

import java.util.UUID;

public final class CreateDocumentItemCommand {
    private final UUID householdId;
    private final String text;

    public CreateDocumentItemCommand(UUID householdId, String text) {
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
