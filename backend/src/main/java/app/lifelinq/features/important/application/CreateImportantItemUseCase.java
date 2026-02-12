package app.lifelinq.features.important.application;

import app.lifelinq.features.important.domain.ImportantItem;
import java.util.UUID;

final class CreateImportantItemUseCase {

    public CreateImportantItemResult execute(CreateImportantItemCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.getHouseholdId() == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        if (command.getText() == null || command.getText().isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }

        ImportantItem item = new ImportantItem(UUID.randomUUID(), command.getHouseholdId(), command.getText());
        return new CreateImportantItemResult(item.getId());
    }
}
