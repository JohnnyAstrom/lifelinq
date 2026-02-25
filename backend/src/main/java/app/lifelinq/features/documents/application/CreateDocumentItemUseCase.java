package app.lifelinq.features.documents.application;

import app.lifelinq.features.documents.domain.DocumentItem;
import app.lifelinq.features.documents.domain.DocumentRepository;
import java.util.UUID;

final class CreateDocumentItemUseCase {
    private final DocumentRepository documentRepository;

    public CreateDocumentItemUseCase(DocumentRepository documentRepository) {
        if (documentRepository == null) {
            throw new IllegalArgumentException("documentRepository must not be null");
        }
        this.documentRepository = documentRepository;
    }

    public CreateDocumentItemResult execute(CreateDocumentItemCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.getGroupId() == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        if (command.getCreatedByUserId() == null) {
            throw new IllegalArgumentException("createdByUserId must not be null");
        }
        if (command.getTitle() == null || command.getTitle().isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (command.getCreatedAt() == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }

        DocumentItem item = new DocumentItem(
                UUID.randomUUID(),
                command.getGroupId(),
                command.getCreatedByUserId(),
                command.getTitle(),
                command.getNotes(),
                command.getDate(),
                command.getCategory(),
                command.getTags(),
                command.getExternalLink(),
                command.getCreatedAt()
        );
        documentRepository.save(item);
        return new CreateDocumentItemResult(item.getId());
    }
}
