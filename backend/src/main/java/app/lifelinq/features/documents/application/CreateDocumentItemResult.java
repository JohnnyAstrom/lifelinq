package app.lifelinq.features.documents.application;

import java.util.UUID;

public final class CreateDocumentItemResult {
    private final UUID itemId;

    public CreateDocumentItemResult(UUID itemId) {
        this.itemId = itemId;
    }

    public UUID getItemId() {
        return itemId;
    }
}
