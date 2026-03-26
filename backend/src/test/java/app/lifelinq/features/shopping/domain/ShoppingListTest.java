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
    void updateIdentityUpdatesNameAndType() {
        ShoppingList list = new ShoppingList(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Groceries",
                ShoppingListType.GROCERY,
                Instant.now()
        );

        list.updateIdentity("Cabin supplies", ShoppingListType.SUPPLIES);

        assertEquals("Cabin supplies", list.getName());
        assertEquals(ShoppingListType.SUPPLIES, list.getType());
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

        ShoppingAddItemResult result = list.addItem(
                UUID.randomUUID(),
                "tomato",
                new BigDecimal("3"),
                ShoppingUnit.PCS,
                ShoppingItemSourceKind.MEAL_PLAN,
                "Soup",
                Instant.now()
        );

        assertEquals(existingId, result.itemId());
        assertEquals(ShoppingAddItemOutcome.INCREASED_EXISTING, result.outcome());
        assertEquals(1, list.getItems().size());
        assertEquals(new BigDecimal("5"), list.getItems().get(0).getQuantity());
        assertNull(list.getItems().get(0).getSourceKind());
        assertNull(list.getItems().get(0).getSourceLabel());
    }

    @Test
    void mealPlanIntakeReusesSingleMatchingQuantifiedOpenItemWhenIncomingHasNoQuantity() {
        ShoppingList list = new ShoppingList(UUID.randomUUID(), UUID.randomUUID(), "List", Instant.now());
        UUID existingId = UUID.randomUUID();
        list.addItem(
                existingId,
                "banana",
                new BigDecimal("3"),
                ShoppingUnit.PCS,
                ShoppingItemSourceKind.MEAL_PLAN,
                "Smoothie",
                Instant.now()
        );

        ShoppingAddItemResult result = list.addItem(
                UUID.randomUUID(),
                "banana",
                null,
                null,
                ShoppingItemSourceKind.MEAL_PLAN,
                "Pancakes",
                Instant.now()
        );

        assertEquals(existingId, result.itemId());
        assertEquals(ShoppingAddItemOutcome.UPDATED_EXISTING, result.outcome());
        assertEquals(1, list.getItems().size());
        assertNull(list.getItems().get(0).getQuantity());
        assertNull(list.getItems().get(0).getUnit());
        assertNull(list.getItems().get(0).getSourceKind());
        assertNull(list.getItems().get(0).getSourceLabel());
    }

    @Test
    void mealPlanIntakeDoesNotMergeWhenMatchingOpenItemsAreAmbiguous() {
        ShoppingList list = new ShoppingList(UUID.randomUUID(), UUID.randomUUID(), "List", Instant.now());
        list.addItem(UUID.randomUUID(), "tomato", null, null, null, null, true, Instant.now());
        list.addItem(UUID.randomUUID(), "tomato", null, null, null, null, true, Instant.now());

        ShoppingAddItemResult result = list.addItem(
                UUID.randomUUID(),
                "tomato",
                null,
                null,
                ShoppingItemSourceKind.MEAL_PLAN,
                "Salad",
                Instant.now()
        );

        assertEquals(3, list.getItems().size());
        assertEquals(result.itemId(), list.getItems().get(0).getId());
        assertEquals(ShoppingAddItemOutcome.CREATED, result.outcome());
    }

    @Test
    void mealPlanIntakeDoesNotMergeWhenQuantityCompatibilityIsUnclear() {
        ShoppingList list = new ShoppingList(UUID.randomUUID(), UUID.randomUUID(), "List", Instant.now());
        UUID existingId = UUID.randomUUID();
        list.addItem(existingId, "tomato", Instant.now());

        ShoppingAddItemResult result = list.addItem(
                UUID.randomUUID(),
                "tomato",
                new BigDecimal("2"),
                ShoppingUnit.PCS,
                ShoppingItemSourceKind.MEAL_PLAN,
                "Pasta",
                Instant.now()
        );

        assertEquals(existingId, result.itemId());
        assertEquals(1, list.getItems().size());
        assertEquals(ShoppingAddItemOutcome.UPDATED_EXISTING, result.outcome());
        assertNull(list.getItems().get(0).getQuantity());
        assertNull(list.getItems().get(0).getUnit());
    }

    @Test
    void mealPlanIntakeDropsFalsePrecisionWhenUnitsDiffer() {
        ShoppingList list = new ShoppingList(UUID.randomUUID(), UUID.randomUUID(), "List", Instant.now());
        UUID existingId = UUID.randomUUID();
        list.addItem(
                existingId,
                "olive oil",
                new BigDecimal("30"),
                ShoppingUnit.ML,
                ShoppingItemSourceKind.MEAL_PLAN,
                "Pasta",
                Instant.now()
        );

        ShoppingAddItemResult result = list.addItem(
                UUID.randomUUID(),
                "olive oil",
                new BigDecimal("1"),
                ShoppingUnit.L,
                ShoppingItemSourceKind.MEAL_PLAN,
                "Salad",
                Instant.now()
        );

        assertEquals(existingId, result.itemId());
        assertEquals(ShoppingAddItemOutcome.UPDATED_EXISTING, result.outcome());
        assertEquals(1, list.getItems().size());
        assertNull(list.getItems().get(0).getQuantity());
        assertNull(list.getItems().get(0).getUnit());
    }

    @Test
    void mealPlanIntakeDoesNotMergeIntoBoughtItem() {
        ShoppingList list = new ShoppingList(UUID.randomUUID(), UUID.randomUUID(), "List", Instant.now());
        UUID boughtId = UUID.randomUUID();
        list.addItem(
                boughtId,
                "banana",
                new BigDecimal("3"),
                ShoppingUnit.PCS,
                ShoppingItemSourceKind.MEAL_PLAN,
                "Smoothie",
                Instant.now()
        );
        list.toggleItem(boughtId, Instant.now());

        ShoppingAddItemResult result = list.addItem(
                UUID.randomUUID(),
                "banana",
                null,
                null,
                ShoppingItemSourceKind.MEAL_PLAN,
                "Pancakes",
                Instant.now()
        );

        assertEquals(2, list.getItems().size());
        assertEquals(ShoppingAddItemOutcome.CREATED, result.outcome());
    }

    @Test
    void manualAddReusesSingleMatchingOpenItemWithoutQuantity() {
        ShoppingList list = new ShoppingList(UUID.randomUUID(), UUID.randomUUID(), "List", Instant.now());
        UUID existingId = UUID.randomUUID();
        list.addItem(existingId, "banana", Instant.now());

        ShoppingAddItemResult result = list.addItem(UUID.randomUUID(), "banana", Instant.now());

        assertEquals(existingId, result.itemId());
        assertEquals(ShoppingAddItemOutcome.REUSED_EXISTING, result.outcome());
        assertEquals(1, list.getItems().size());
    }

    @Test
    void manualAddUpdatesSingleMatchingOpenItemWithIncomingQuantityWhenExistingHasNone() {
        ShoppingList list = new ShoppingList(UUID.randomUUID(), UUID.randomUUID(), "List", Instant.now());
        UUID existingId = UUID.randomUUID();
        list.addItem(existingId, "tomato", Instant.now());

        ShoppingAddItemResult result = list.addItem(
                UUID.randomUUID(),
                "tomato",
                new BigDecimal("3"),
                ShoppingUnit.PCS,
                null,
                null,
                false,
                Instant.now()
        );

        assertEquals(existingId, result.itemId());
        assertEquals(ShoppingAddItemOutcome.UPDATED_EXISTING, result.outcome());
        assertEquals(1, list.getItems().size());
        assertEquals(new BigDecimal("3"), list.getItems().get(0).getQuantity());
        assertEquals(ShoppingUnit.PCS, list.getItems().get(0).getUnit());
    }

    @Test
    void manualAddIncreasesSingleMatchingOpenItemWhenQuantityAndUnitAreCompatible() {
        ShoppingList list = new ShoppingList(UUID.randomUUID(), UUID.randomUUID(), "List", Instant.now());
        UUID existingId = UUID.randomUUID();
        list.addItem(existingId, "tomato", new BigDecimal("2"), ShoppingUnit.PCS, Instant.now());

        ShoppingAddItemResult result = list.addItem(
                UUID.randomUUID(),
                "tomato",
                new BigDecimal("3"),
                ShoppingUnit.PCS,
                null,
                null,
                false,
                Instant.now()
        );

        assertEquals(existingId, result.itemId());
        assertEquals(ShoppingAddItemOutcome.INCREASED_EXISTING, result.outcome());
        assertEquals(1, list.getItems().size());
        assertEquals(new BigDecimal("5"), list.getItems().get(0).getQuantity());
    }

    @Test
    void manualAddCreatesNewRowWhenExplicitAddAsNewIsRequested() {
        ShoppingList list = new ShoppingList(UUID.randomUUID(), UUID.randomUUID(), "List", Instant.now());
        list.addItem(UUID.randomUUID(), "banana", Instant.now());

        ShoppingAddItemResult result = list.addItem(
                UUID.randomUUID(),
                "banana",
                null,
                null,
                null,
                null,
                true,
                Instant.now()
        );

        assertEquals(2, list.getItems().size());
        assertEquals(ShoppingAddItemOutcome.CREATED, result.outcome());
    }

    @Test
    void manualAddCreatesNewRowWhenQuantityUnitAreIncompatible() {
        ShoppingList list = new ShoppingList(UUID.randomUUID(), UUID.randomUUID(), "List", Instant.now());
        UUID existingId = UUID.randomUUID();
        list.addItem(existingId, "tomato", new BigDecimal("2"), ShoppingUnit.KG, Instant.now());

        ShoppingAddItemResult result = list.addItem(
                UUID.randomUUID(),
                "tomato",
                new BigDecimal("3"),
                ShoppingUnit.PCS,
                null,
                null,
                false,
                Instant.now()
        );

        assertNotEquals(existingId, result.itemId());
        assertEquals(2, list.getItems().size());
        assertEquals(ShoppingAddItemOutcome.CREATED, result.outcome());
    }
}
