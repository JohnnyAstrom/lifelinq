package app.lifelinq.features.documents.application;

import app.lifelinq.features.documents.domain.DocumentItem;
import app.lifelinq.features.documents.domain.DocumentRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class DocumentsApplicationService {
    private final CreateDocumentItemUseCase createDocumentItemUseCase;
    private final ListDocumentsUseCase listDocumentsUseCase;
    private final DeleteDocumentItemUseCase deleteDocumentItemUseCase;

    public DocumentsApplicationService(
            CreateDocumentItemUseCase createDocumentItemUseCase,
            ListDocumentsUseCase listDocumentsUseCase,
            DeleteDocumentItemUseCase deleteDocumentItemUseCase
    ) {
        if (createDocumentItemUseCase == null) {
            throw new IllegalArgumentException("createDocumentItemUseCase must not be null");
        }
        if (listDocumentsUseCase == null) {
            throw new IllegalArgumentException("listDocumentsUseCase must not be null");
        }
        if (deleteDocumentItemUseCase == null) {
            throw new IllegalArgumentException("deleteDocumentItemUseCase must not be null");
        }
        this.createDocumentItemUseCase = createDocumentItemUseCase;
        this.listDocumentsUseCase = listDocumentsUseCase;
        this.deleteDocumentItemUseCase = deleteDocumentItemUseCase;
    }

    public UUID createDocument(
            UUID householdId,
            UUID actorUserId,
            String title,
            String notes,
            LocalDate date,
            String category,
            List<String> tags,
            String externalLink
    ) {
        CreateDocumentItemResult result = createDocumentItemUseCase.execute(
                new CreateDocumentItemCommand(
                        householdId,
                        actorUserId,
                        title,
                        notes,
                        date,
                        category,
                        tags,
                        externalLink,
                        Instant.now()
                )
        );
        return result.getItemId();
    }

    public List<DocumentItem> listDocuments(UUID householdId, Optional<String> q) {
        return listDocumentsUseCase.execute(householdId, q);
    }

    public boolean deleteDocument(UUID householdId, UUID documentId) {
        return deleteDocumentItemUseCase.execute(householdId, documentId);
    }

    public static DocumentsApplicationService create(DocumentRepository documentRepository) {
        return new DocumentsApplicationService(
                new CreateDocumentItemUseCase(documentRepository),
                new ListDocumentsUseCase(documentRepository),
                new DeleteDocumentItemUseCase(documentRepository)
        );
    }
}
