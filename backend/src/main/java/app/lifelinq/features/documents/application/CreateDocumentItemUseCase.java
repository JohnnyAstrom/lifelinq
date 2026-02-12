package app.lifelinq.features.documents.application;

import app.lifelinq.features.documents.domain.DocumentItem;
import java.util.UUID;

final class CreateDocumentItemUseCase {

    public CreateDocumentItemResult execute(CreateDocumentItemCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.getHouseholdId() == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        if (command.getText() == null || command.getText().isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }

        DocumentItem item = new DocumentItem(UUID.randomUUID(), command.getHouseholdId(), command.getText());
        return new CreateDocumentItemResult(item.getId());
    }
}
