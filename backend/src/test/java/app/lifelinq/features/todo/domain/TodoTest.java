package app.lifelinq.features.todo.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TodoTest {

    @Test
    void requiresId() {
        assertThrows(IllegalArgumentException.class, () -> new Todo(null, UUID.randomUUID(), "Task"));
    }

    @Test
    void requiresHouseholdId() {
        assertThrows(IllegalArgumentException.class, () -> new Todo(UUID.randomUUID(), null, "Task"));
    }

    @Test
    void requiresNonBlankText() {
        assertThrows(IllegalArgumentException.class, () -> new Todo(UUID.randomUUID(), UUID.randomUUID(), " "));
    }

    @Test
    void acceptsValidInput() {
        assertDoesNotThrow(() -> new Todo(UUID.randomUUID(), UUID.randomUUID(), "Task"));
    }

    @Test
    void togglesToCompletedWhenOpen() {
        Todo todo = new Todo(UUID.randomUUID(), UUID.randomUUID(), "Task");

        todo.toggle(Instant.parse("2026-01-01T00:00:00Z"));

        assertEquals(TodoStatus.COMPLETED, todo.getStatus());
    }

    @Test
    void togglesBackToOpenWhenCompleted() {
        Todo todo = new Todo(UUID.randomUUID(), UUID.randomUUID(), "Task");
        todo.toggle(Instant.parse("2026-01-01T00:00:00Z"));

        todo.toggle(Instant.parse("2026-01-02T00:00:00Z"));

        assertEquals(TodoStatus.OPEN, todo.getStatus());
    }
}
