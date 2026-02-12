package app.lifelinq.features.important.application;

import java.util.UUID;

public final class CreateImportantItemResult {
    private final UUID itemId;

    public CreateImportantItemResult(UUID itemId) {
        this.itemId = itemId;
    }

    public UUID getItemId() {
        return itemId;
    }
}
