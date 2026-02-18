package app.lifelinq.features.shopping.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ShoppingListTest {

    @Test
    void addItemPlacesNewestItemAtTop() {
        ShoppingList list = new ShoppingList(UUID.randomUUID(), UUID.randomUUID(), "List", Instant.now());
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        UUID third = UUID.randomUUID();
        list.addItem(first, "milk", Instant.now());
        list.addItem(second, "bread", Instant.now());
        list.addItem(third, "eggs", Instant.now());

        List<ShoppingItem> items = list.getItems();
        assertEquals(third, items.get(0).getId());
        assertEquals(second, items.get(1).getId());
        assertEquals(first, items.get(2).getId());
    }

    @Test
    void reorderOpenItemSwapsWithNeighbor() {
        ShoppingList list = new ShoppingList(UUID.randomUUID(), UUID.randomUUID(), "List", Instant.now());
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        UUID third = UUID.randomUUID();
        list.addItem(first, "milk", Instant.now());
        list.addItem(second, "bread", Instant.now());
        list.addItem(third, "eggs", Instant.now());

        list.reorderOpenItem(second, "UP");

        List<ShoppingItem> items = list.getItems();
        assertEquals(second, items.get(0).getId());
        assertEquals(third, items.get(1).getId());
        assertEquals(first, items.get(2).getId());
    }

    @Test
    void reorderOpenItemDoesNothingAtBoundaries() {
        ShoppingList list = new ShoppingList(UUID.randomUUID(), UUID.randomUUID(), "List", Instant.now());
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        list.addItem(first, "milk", Instant.now());
        list.addItem(second, "bread", Instant.now());

        list.reorderOpenItem(first, "UP");
        list.reorderOpenItem(second, "DOWN");

        List<ShoppingItem> items = list.getItems();
        assertEquals(first, items.get(0).getId());
        assertEquals(second, items.get(1).getId());
    }

    @Test
    void reorderOpenItemRejectsBoughtItems() {
        ShoppingList list = new ShoppingList(UUID.randomUUID(), UUID.randomUUID(), "List", Instant.now());
        UUID open = UUID.randomUUID();
        UUID bought = UUID.randomUUID();
        list.addItem(open, "milk", Instant.now());
        list.addItem(bought, "bread", Instant.now());
        list.toggleItem(bought, Instant.now());

        assertThrows(ShoppingItemNotFoundException.class, () -> list.reorderOpenItem(bought, "UP"));
    }
}
