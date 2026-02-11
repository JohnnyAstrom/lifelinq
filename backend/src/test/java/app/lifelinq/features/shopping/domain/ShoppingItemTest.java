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
        UUID householdId = UUID.randomUUID();
        Instant createdAt = Instant.now();

        ShoppingItem item = new ShoppingItem(id, householdId, "Milk", createdAt);

        assertEquals(id, item.getId());
        assertEquals(householdId, item.getHouseholdId());
        assertEquals("Milk", item.getName());
        assertEquals(createdAt, item.getCreatedAt());
    }

    @Test
    void rejectsNullId() {
        assertThrows(IllegalArgumentException.class, () ->
                new ShoppingItem(null, UUID.randomUUID(), "Milk", Instant.now())
        );
    }

    @Test
    void rejectsNullHouseholdId() {
        assertThrows(IllegalArgumentException.class, () ->
                new ShoppingItem(UUID.randomUUID(), null, "Milk", Instant.now())
        );
    }

    @Test
    void rejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () ->
                new ShoppingItem(UUID.randomUUID(), UUID.randomUUID(), " ", Instant.now())
        );
    }

    @Test
    void rejectsNullCreatedAt() {
        assertThrows(IllegalArgumentException.class, () ->
                new ShoppingItem(UUID.randomUUID(), UUID.randomUUID(), "Milk", null)
        );
    }
}
