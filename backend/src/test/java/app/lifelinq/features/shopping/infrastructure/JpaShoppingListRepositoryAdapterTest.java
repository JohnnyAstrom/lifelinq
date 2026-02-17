package app.lifelinq.features.shopping.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.shopping.domain.ShoppingItem;
import app.lifelinq.features.shopping.domain.ShoppingItemStatus;
import app.lifelinq.features.shopping.domain.ShoppingList;
import app.lifelinq.features.shopping.domain.ShoppingListRepository;
import app.lifelinq.features.shopping.domain.ShoppingUnit;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = ShoppingJpaTestApplication.class)
@ActiveProfiles("test")
class JpaShoppingListRepositoryAdapterTest {

    @Autowired
    private ShoppingListRepository repository;

    @Test
    void savesAndLoadsShoppingListRoundTrip() {
        UUID householdId = UUID.randomUUID();
        UUID listId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        ShoppingList list = new ShoppingList(listId, householdId, "Groceries", 3, createdAt);
        UUID itemId = UUID.randomUUID();
        Instant itemCreatedAt = createdAt.plusSeconds(5);
        list.addItem(itemId, "milk", new BigDecimal("2"), ShoppingUnit.DL, itemCreatedAt);
        Instant boughtAt = itemCreatedAt.plusSeconds(10);
        list.toggleItem(itemId, boughtAt);

        repository.save(list);

        Optional<ShoppingList> loaded = repository.findById(listId);
        assertTrue(loaded.isPresent());
        ShoppingList loadedList = loaded.get();
        assertEquals(listId, loadedList.getId());
        assertEquals(householdId, loadedList.getHouseholdId());
        assertEquals("Groceries", loadedList.getName());
        assertEquals(3, loadedList.getOrderIndex());
        long diffNanos = Math.abs(Duration.between(createdAt, loadedList.getCreatedAt()).toNanos());
        assertTrue(diffNanos <= 1_000);
        assertEquals(1, loadedList.getItems().size());
        ShoppingItem loadedItem = loadedList.getItems().get(0);
        assertEquals(itemId, loadedItem.getId());
        assertEquals("milk", loadedItem.getName());
        assertEquals(ShoppingItemStatus.BOUGHT, loadedItem.getStatus());
        assertEquals(0, loadedItem.getQuantity().compareTo(new BigDecimal("2")));
        assertEquals(ShoppingUnit.DL, loadedItem.getUnit());
        long boughtDiffNanos = Math.abs(Duration.between(boughtAt, loadedItem.getBoughtAt()).toNanos());
        assertTrue(boughtDiffNanos <= 1_000);
    }

    @Test
    void findsByHouseholdId() {
        UUID householdId = UUID.randomUUID();
        ShoppingList list = new ShoppingList(UUID.randomUUID(), householdId, "Groceries", Instant.now());
        repository.save(list);

        List<ShoppingList> lists = repository.findByHouseholdId(householdId);
        assertEquals(1, lists.size());
        assertEquals(list.getId(), lists.get(0).getId());
    }
}
