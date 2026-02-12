package app.lifelinq.features.shopping.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.shopping.domain.ShoppingList;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InMemoryShoppingListRepositoryTest {

    @Test
    void savesLists() {
        InMemoryShoppingListRepository repository = new InMemoryShoppingListRepository();
        ShoppingList list = new ShoppingList(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Groceries",
                Instant.now()
        );

        repository.save(list);

        assertEquals(1, repository.size());
    }

    @Test
    void findsById() {
        InMemoryShoppingListRepository repository = new InMemoryShoppingListRepository();
        ShoppingList list = new ShoppingList(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Groceries",
                Instant.now()
        );

        repository.save(list);

        assertTrue(repository.findById(list.getId()).isPresent());
    }

    @Test
    void findsByHouseholdId() {
        InMemoryShoppingListRepository repository = new InMemoryShoppingListRepository();
        UUID householdId = UUID.randomUUID();
        ShoppingList list = new ShoppingList(
                UUID.randomUUID(),
                householdId,
                "Groceries",
                Instant.now()
        );

        repository.save(list);

        List<ShoppingList> result = repository.findByHouseholdId(householdId);
        assertEquals(1, result.size());
    }

    @Test
    void rejectsNullList() {
        InMemoryShoppingListRepository repository = new InMemoryShoppingListRepository();

        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
    }

    @Test
    void rejectsNullId() {
        InMemoryShoppingListRepository repository = new InMemoryShoppingListRepository();

        assertThrows(IllegalArgumentException.class, () -> repository.findById(null));
    }
}
