package app.lifelinq.features.shopping.application;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.lifelinq.features.shopping.domain.ShoppingItem;
import app.lifelinq.features.shopping.domain.ShoppingItemRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AddShoppingItemUseCaseTest {

    @Test
    void returnsItemId() {
        FakeShoppingItemRepository repository = new FakeShoppingItemRepository();
        AddShoppingItemUseCase useCase = new AddShoppingItemUseCase(repository);
        AddShoppingItemCommand command = new AddShoppingItemCommand(UUID.randomUUID(), "Milk");

        AddShoppingItemResult result = useCase.execute(command);

        assertNotNull(result.getItemId());
        assertEquals(1, repository.saveCount);
    }

    @Test
    void rejectsNullCommand() {
        AddShoppingItemUseCase useCase = new AddShoppingItemUseCase(new FakeShoppingItemRepository());

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    @Test
    void rejectsNullHouseholdId() {
        AddShoppingItemUseCase useCase = new AddShoppingItemUseCase(new FakeShoppingItemRepository());
        AddShoppingItemCommand command = new AddShoppingItemCommand(null, "Milk");

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }

    @Test
    void rejectsBlankName() {
        AddShoppingItemUseCase useCase = new AddShoppingItemUseCase(new FakeShoppingItemRepository());
        AddShoppingItemCommand command = new AddShoppingItemCommand(UUID.randomUUID(), " ");

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }

    private static final class FakeShoppingItemRepository implements ShoppingItemRepository {
        private int saveCount;

        @Override
        public void save(ShoppingItem item) {
            saveCount++;
        }
    }
}
