package app.lifelinq.features.todo.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalDate;
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

        Instant completedAt = Instant.parse("2026-01-01T00:00:00Z");
        todo.toggle(completedAt);

        assertEquals(TodoStatus.COMPLETED, todo.getStatus());
        assertEquals(completedAt, todo.getCompletedAt());
    }

    @Test
    void togglesBackToOpenWhenCompleted() {
        Todo todo = new Todo(UUID.randomUUID(), UUID.randomUUID(), "Task");
        todo.toggle(Instant.parse("2026-01-01T00:00:00Z"));

        todo.toggle(Instant.parse("2026-01-02T00:00:00Z"));

        assertEquals(TodoStatus.OPEN, todo.getStatus());
        assertEquals(null, todo.getCompletedAt());
    }

    @Test
    void requiresDueDateForDayScope() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Todo(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Task",
                        TodoScope.DAY,
                        null,
                        null,
                        null,
                        null,
                        null,
                        Instant.now()
                )
        );
    }

    @Test
    void rejectsDueDateForLaterScope() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Todo(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Task",
                        TodoScope.LATER,
                        LocalDate.of(2026, 2, 1),
                        null,
                        null,
                        null,
                        null,
                        Instant.now()
                )
        );
    }
}
