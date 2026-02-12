package app.lifelinq.features.shopping.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
                createdAt,
                ShoppingItemStatus.BOUGHT,
                boughtAt
        );
        assertEquals(ShoppingItemStatus.BOUGHT, bought.getStatus());
        assertEquals(boughtAt, bought.getBoughtAt());

        assertThrows(IllegalArgumentException.class, () ->
                ShoppingItem.rehydrate(
                        UUID.randomUUID(),
                        "Milk",
                        createdAt,
                        ShoppingItemStatus.BOUGHT,
                        null
                )
        );
        assertThrows(IllegalArgumentException.class, () ->
                ShoppingItem.rehydrate(
                        UUID.randomUUID(),
                        "Milk",
                        createdAt,
                        ShoppingItemStatus.TO_BUY,
                        Instant.now()
                )
        );
    }
}
