package app.lifelinq.features.important.application;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class CreateImportantItemUseCaseTest {

    @Test
    void createsItemAndReturnsId() {
        CreateImportantItemUseCase useCase = new CreateImportantItemUseCase();
        CreateImportantItemCommand command = new CreateImportantItemCommand(UUID.randomUUID(), "Pay rent");

        CreateImportantItemResult result = useCase.execute(command);

        assertNotNull(result);
        assertNotNull(result.getItemId());
    }

    @Test
    void requiresCommand() {
        CreateImportantItemUseCase useCase = new CreateImportantItemUseCase();
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    @Test
    void requiresHouseholdId() {
        CreateImportantItemUseCase useCase = new CreateImportantItemUseCase();
        CreateImportantItemCommand command = new CreateImportantItemCommand(null, "Pay rent");
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }

    @Test
    void requiresText() {
        CreateImportantItemUseCase useCase = new CreateImportantItemUseCase();
        CreateImportantItemCommand command = new CreateImportantItemCommand(UUID.randomUUID(), " ");
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }
}
