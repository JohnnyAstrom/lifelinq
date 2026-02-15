package app.lifelinq.features.documents.application;

import java.util.UUID;

public class DocumentsApplicationService {
    private final CreateDocumentItemUseCase createDocumentItemUseCase;

    public DocumentsApplicationService(CreateDocumentItemUseCase createDocumentItemUseCase) {
        if (createDocumentItemUseCase == null) {
            throw new IllegalArgumentException("createDocumentItemUseCase must not be null");
        }
        this.createDocumentItemUseCase = createDocumentItemUseCase;
    }

    public UUID createDocumentItem(UUID householdId, String text) {
        CreateDocumentItemResult result = createDocumentItemUseCase.execute(
                new CreateDocumentItemCommand(householdId, text)
        );
        return result.getItemId();
    }

    public static DocumentsApplicationService create() {
        return new DocumentsApplicationService(new CreateDocumentItemUseCase());
    }
}
