package app.lifelinq.features.shopping.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.shopping.domain.ShoppingItem;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InMemoryShoppingItemRepositoryTest {

    @Test
    void savesItems() {
        InMemoryShoppingItemRepository repository = new InMemoryShoppingItemRepository();
        ShoppingItem item = new ShoppingItem(UUID.randomUUID(), UUID.randomUUID(), "Milk", Instant.now());

        repository.save(item);

        assertEquals(1, repository.size());
    }

    @Test
    void findsById() {
        InMemoryShoppingItemRepository repository = new InMemoryShoppingItemRepository();
        ShoppingItem item = new ShoppingItem(UUID.randomUUID(), UUID.randomUUID(), "Milk", Instant.now());

        repository.save(item);

        assertTrue(repository.findById(item.getId()).isPresent());
    }

    @Test
    void rejectsNullItem() {
        InMemoryShoppingItemRepository repository = new InMemoryShoppingItemRepository();

        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
    }

    @Test
    void rejectsNullId() {
        InMemoryShoppingItemRepository repository = new InMemoryShoppingItemRepository();

        assertThrows(IllegalArgumentException.class, () -> repository.findById(null));
    }
}
