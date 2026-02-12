package app.lifelinq.features.documents.application;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class CreateDocumentItemUseCaseTest {

    @Test
    void createsItemAndReturnsId() {
        CreateDocumentItemUseCase useCase = new CreateDocumentItemUseCase();
        CreateDocumentItemCommand command = new CreateDocumentItemCommand(UUID.randomUUID(), "Pay rent");

        CreateDocumentItemResult result = useCase.execute(command);

        assertNotNull(result);
        assertNotNull(result.getItemId());
    }

    @Test
    void requiresCommand() {
        CreateDocumentItemUseCase useCase = new CreateDocumentItemUseCase();
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    @Test
    void requiresHouseholdId() {
        CreateDocumentItemUseCase useCase = new CreateDocumentItemUseCase();
        CreateDocumentItemCommand command = new CreateDocumentItemCommand(null, "Pay rent");
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }

    @Test
    void requiresText() {
        CreateDocumentItemUseCase useCase = new CreateDocumentItemUseCase();
        CreateDocumentItemCommand command = new CreateDocumentItemCommand(UUID.randomUUID(), " ");
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }
}
