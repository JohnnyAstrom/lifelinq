package app.lifelinq.features.documents.api;

import java.util.UUID;

public final class CreateDocumentResponse {
    private final UUID documentId;

    public CreateDocumentResponse(UUID documentId) {
        this.documentId = documentId;
    }

    public UUID getDocumentId() {
        return documentId;
    }
}
