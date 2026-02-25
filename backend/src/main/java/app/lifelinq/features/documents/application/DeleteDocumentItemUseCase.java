package app.lifelinq.features.documents.application;

import app.lifelinq.features.documents.domain.DocumentRepository;
import java.util.UUID;

final class DeleteDocumentItemUseCase {
    private final DocumentRepository documentRepository;

    DeleteDocumentItemUseCase(DocumentRepository documentRepository) {
        if (documentRepository == null) {
            throw new IllegalArgumentException("documentRepository must not be null");
        }
        this.documentRepository = documentRepository;
    }

    boolean execute(UUID groupId, UUID documentId) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        if (documentId == null) {
            throw new IllegalArgumentException("documentId must not be null");
        }
        return documentRepository.deleteByIdAndGroupId(documentId, groupId);
    }
}
