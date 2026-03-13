package app.lifelinq.features.shopping.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ShoppingItemTest {

    @Test
    void createsValidItem() {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now();

        ShoppingItem item = new ShoppingItem(id, "Milk", createdAt);

        assertEquals(id, item.getId());
        assertEquals("Milk", item.getName());
        assertEquals(createdAt, item.getCreatedAt());
        assertEquals(ShoppingItemStatus.TO_BUY, item.getStatus());
        assertEquals(null, item.getBoughtAt());
        assertEquals(null, item.getQuantity());
        assertEquals(null, item.getUnit());
    }

    @Test
    void rejectsNullId() {
        assertThrows(IllegalArgumentException.class, () ->
                new ShoppingItem(null, "Milk", Instant.now())
        );
    }

    @Test
    void rejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () ->
                new ShoppingItem(UUID.randomUUID(), " ", Instant.now())
        );
    }

    @Test
    void rejectsNullCreatedAt() {
        assertThrows(IllegalArgumentException.class, () ->
                new ShoppingItem(UUID.randomUUID(), "Milk", null)
        );
    }

    @Test
    void toggleSetsAndClearsBoughtAt() {
        ShoppingItem item = new ShoppingItem(UUID.randomUUID(), "Milk", Instant.now());
        Instant boughtAt = Instant.now().plusSeconds(5);

        item.toggle(boughtAt);
        assertEquals(ShoppingItemStatus.BOUGHT, item.getStatus());
        assertEquals(boughtAt, item.getBoughtAt());

        item.toggle(Instant.now().plusSeconds(10));
        assertEquals(ShoppingItemStatus.TO_BUY, item.getStatus());
        assertEquals(null, item.getBoughtAt());
    }

    @Test
    void rehydrateValidatesStatusAndBoughtAt() {
        Instant createdAt = Instant.now();
        Instant boughtAt = createdAt.plusSeconds(60);

        ShoppingItem bought = ShoppingItem.rehydrate(
                UUID.randomUUID(),
                "Milk",
                0,
                createdAt,
                ShoppingItemStatus.BOUGHT,
                boughtAt,
                new BigDecimal("2.5"),
                ShoppingUnit.DL,
                null,
                null
        );
        assertEquals(ShoppingItemStatus.BOUGHT, bought.getStatus());
        assertEquals(boughtAt, bought.getBoughtAt());
        assertEquals(new BigDecimal("2.5"), bought.getQuantity());
        assertEquals(ShoppingUnit.DL, bought.getUnit());

        assertThrows(IllegalArgumentException.class, () ->
                ShoppingItem.rehydrate(
                        UUID.randomUUID(),
                        "Milk",
                        0,
                        createdAt,
                        ShoppingItemStatus.BOUGHT,
                        null,
                        null,
                        null,
                        null,
                        null
                )
        );
        assertThrows(IllegalArgumentException.class, () ->
                ShoppingItem.rehydrate(
                        UUID.randomUUID(),
                        "Milk",
                        0,
                        createdAt,
                        ShoppingItemStatus.TO_BUY,
                        Instant.now(),
                        null,
                        null,
                        null,
                        null
                )
        );
    }

    @Test
    void rejectsQuantityWithoutUnit() {
        assertThrows(IllegalArgumentException.class, () ->
                new ShoppingItem(UUID.randomUUID(), "Milk", Instant.now(), new BigDecimal("1"), null)
        );
    }

    @Test
    void rejectsUnitWithoutQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
                new ShoppingItem(UUID.randomUUID(), "Milk", Instant.now(), null, ShoppingUnit.PCS)
        );
    }

    @Test
    void rejectsNonPositiveQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
                new ShoppingItem(UUID.randomUUID(), "Milk", Instant.now(), BigDecimal.ZERO, ShoppingUnit.PCS)
        );
    }

    @Test
    void rejectsSourceLabelWithoutSourceKind() {
        assertThrows(IllegalArgumentException.class, () ->
                new ShoppingItem(UUID.randomUUID(), "Milk", Instant.now(), null, null, null, "Pasta")
        );
    }

    @Test
    void absorbsCompatibleMealPlanQuantityAndClearsSource() {
        ShoppingItem item = new ShoppingItem(
                UUID.randomUUID(),
                "milk",
                Instant.now(),
                new BigDecimal("2"),
                ShoppingUnit.PCS,
                ShoppingItemSourceKind.MEAL_PLAN,
                "Pasta"
        );

        item.absorbMealPlanIntake(new BigDecimal("3"), ShoppingUnit.PCS);

        assertEquals(new BigDecimal("5"), item.getQuantity());
        assertEquals(ShoppingUnit.PCS, item.getUnit());
        assertEquals(null, item.getSourceKind());
        assertEquals(null, item.getSourceLabel());
    }

    @Test
    void rejectsIncompatibleMealPlanAbsorb() {
        ShoppingItem item = new ShoppingItem(
                UUID.randomUUID(),
                "milk",
                Instant.now(),
                new BigDecimal("2"),
                ShoppingUnit.PCS
        );

        assertThrows(IllegalArgumentException.class, () ->
                item.absorbMealPlanIntake(new BigDecimal("3"), ShoppingUnit.KG)
        );
    }
}
