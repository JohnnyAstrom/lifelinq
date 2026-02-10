package app.lifelinq.features.household.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class HouseholdTest {

    @Test
    void requiresId() {
        assertThrows(IllegalArgumentException.class, () -> new Household(null, "Home"));
    }

    @Test
    void allowsNullName() {
        assertDoesNotThrow(() -> new Household(UUID.randomUUID(), null));
    }

    @Test
    void allowsBlankName() {
        assertDoesNotThrow(() -> new Household(UUID.randomUUID(), "  "));
    }
}
