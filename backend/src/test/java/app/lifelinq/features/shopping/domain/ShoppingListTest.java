package app.lifelinq.features.shopping.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
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

    @Test
    void mealPlanIntakeMergesIntoSingleCompatibleOpenItem() {
        ShoppingList list = new ShoppingList(UUID.randomUUID(), UUID.randomUUID(), "List", Instant.now());
        UUID existingId = UUID.randomUUID();
        list.addItem(
                existingId,
                "tomato",
                new BigDecimal("2"),
                ShoppingUnit.PCS,
                ShoppingItemSourceKind.MEAL_PLAN,
                "Pasta",
                Instant.now()
        );

        UUID returnedId = list.addItem(
                UUID.randomUUID(),
                "tomato",
                new BigDecimal("3"),
                ShoppingUnit.PCS,
                ShoppingItemSourceKind.MEAL_PLAN,
                "Soup",
                Instant.now()
        );

        assertEquals(existingId, returnedId);
        assertEquals(1, list.getItems().size());
        assertEquals(new BigDecimal("5"), list.getItems().get(0).getQuantity());
        assertNull(list.getItems().get(0).getSourceKind());
        assertNull(list.getItems().get(0).getSourceLabel());
    }

    @Test
    void mealPlanIntakeDoesNotMergeWhenMatchingOpenItemsAreAmbiguous() {
        ShoppingList list = new ShoppingList(UUID.randomUUID(), UUID.randomUUID(), "List", Instant.now());
        list.addItem(UUID.randomUUID(), "tomato", Instant.now());
        list.addItem(UUID.randomUUID(), "tomato", Instant.now());

        UUID returnedId = list.addItem(
                UUID.randomUUID(),
                "tomato",
                null,
                null,
                ShoppingItemSourceKind.MEAL_PLAN,
                "Salad",
                Instant.now()
        );

        assertEquals(3, list.getItems().size());
        assertEquals(returnedId, list.getItems().get(0).getId());
    }

    @Test
    void mealPlanIntakeDoesNotMergeWhenQuantityCompatibilityIsUnclear() {
        ShoppingList list = new ShoppingList(UUID.randomUUID(), UUID.randomUUID(), "List", Instant.now());
        UUID existingId = UUID.randomUUID();
        list.addItem(existingId, "tomato", Instant.now());

        UUID returnedId = list.addItem(
                UUID.randomUUID(),
                "tomato",
                new BigDecimal("2"),
                ShoppingUnit.PCS,
                ShoppingItemSourceKind.MEAL_PLAN,
                "Pasta",
                Instant.now()
        );

        assertEquals(2, list.getItems().size());
        assertNotEquals(existingId, returnedId);
        assertEquals(returnedId, list.getItems().get(0).getId());
    }
}
