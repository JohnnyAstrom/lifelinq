package app.lifelinq.features.documents.application;

import app.lifelinq.features.documents.domain.DocumentItem;
import app.lifelinq.features.documents.domain.DocumentRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

final class ListDocumentsUseCase {
    private final DocumentRepository documentRepository;

    public ListDocumentsUseCase(DocumentRepository documentRepository) {
        if (documentRepository == null) {
            throw new IllegalArgumentException("documentRepository must not be null");
        }
        this.documentRepository = documentRepository;
    }

    public List<DocumentItem> execute(UUID groupId, Optional<String> q) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        Optional<String> normalizedQuery = (q == null ? Optional.<String>empty() : q)
                .map(String::trim)
                .filter(value -> !value.isEmpty());
        return documentRepository.findByGroupId(groupId, normalizedQuery);
    }
}
