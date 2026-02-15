package app.lifelinq.features.documents.application;

import app.lifelinq.features.documents.domain.DocumentItem;
import app.lifelinq.features.documents.domain.DocumentRepository;
import java.util.List;
import java.util.UUID;

final class ListDocumentsUseCase {
    private final DocumentRepository documentRepository;

    public ListDocumentsUseCase(DocumentRepository documentRepository) {
        if (documentRepository == null) {
            throw new IllegalArgumentException("documentRepository must not be null");
        }
        this.documentRepository = documentRepository;
    }

    public List<DocumentItem> execute(UUID householdId) {
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        return documentRepository.findByHouseholdId(householdId);
    }
}
