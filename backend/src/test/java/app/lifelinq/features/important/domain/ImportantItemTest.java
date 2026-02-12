package app.lifelinq.features.important.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ImportantItemTest {

    @Test
    void createsItemWhenValid() {
        UUID id = UUID.randomUUID();
        UUID householdId = UUID.randomUUID();
        ImportantItem item = new ImportantItem(id, householdId, "Pay rent");

        assertEquals(id, item.getId());
        assertEquals(householdId, item.getHouseholdId());
        assertEquals("Pay rent", item.getText());
    }

    @Test
    void requiresId() {
        assertThrows(IllegalArgumentException.class,
                () -> new ImportantItem(null, UUID.randomUUID(), "Pay rent"));
    }

    @Test
    void requiresHouseholdId() {
        assertThrows(IllegalArgumentException.class,
                () -> new ImportantItem(UUID.randomUUID(), null, "Pay rent"));
    }

    @Test
    void requiresText() {
        assertThrows(IllegalArgumentException.class,
                () -> new ImportantItem(UUID.randomUUID(), UUID.randomUUID(), " "));
    }
}
