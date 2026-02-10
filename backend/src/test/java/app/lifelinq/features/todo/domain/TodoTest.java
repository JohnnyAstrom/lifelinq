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
    void completesWhenOpen() {
        Todo todo = new Todo(UUID.randomUUID(), UUID.randomUUID(), "Task");

        boolean completed = todo.complete(Instant.parse("2026-01-01T00:00:00Z"));

        assertEquals(true, completed);
        assertEquals(TodoStatus.COMPLETED, todo.getStatus());
    }

    @Test
    void completeReturnsFalseWhenAlreadyCompleted() {
        Todo todo = new Todo(UUID.randomUUID(), UUID.randomUUID(), "Task");
        todo.complete(Instant.parse("2026-01-01T00:00:00Z"));

        boolean completed = todo.complete(Instant.parse("2026-01-02T00:00:00Z"));

        assertEquals(false, completed);
        assertEquals(TodoStatus.COMPLETED, todo.getStatus());
    }
}
