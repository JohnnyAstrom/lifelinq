package app.lifelinq.features.shopping.application;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class AddShoppingItemUseCaseTest {

    @Test
    void returnsItemId() {
        AddShoppingItemUseCase useCase = new AddShoppingItemUseCase();
        AddShoppingItemCommand command = new AddShoppingItemCommand(UUID.randomUUID(), "Milk");

        AddShoppingItemResult result = useCase.execute(command);

        assertNotNull(result.getItemId());
    }

    @Test
    void rejectsNullCommand() {
        AddShoppingItemUseCase useCase = new AddShoppingItemUseCase();

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    @Test
    void rejectsNullHouseholdId() {
        AddShoppingItemUseCase useCase = new AddShoppingItemUseCase();
        AddShoppingItemCommand command = new AddShoppingItemCommand(null, "Milk");

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }

    @Test
    void rejectsBlankName() {
        AddShoppingItemUseCase useCase = new AddShoppingItemUseCase();
        AddShoppingItemCommand command = new AddShoppingItemCommand(UUID.randomUUID(), " ");

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }
}
