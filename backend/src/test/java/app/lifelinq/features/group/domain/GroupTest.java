package app.lifelinq.features.group.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class GroupTest {

    @Test
    void requiresId() {
        assertThrows(IllegalArgumentException.class, () -> new Group(null, "Home"));
    }

    @Test
    void allowsNullName() {
        assertDoesNotThrow(() -> new Group(UUID.randomUUID(), null));
    }

    @Test
    void allowsBlankName() {
        assertDoesNotThrow(() -> new Group(UUID.randomUUID(), "  "));
    }
}
