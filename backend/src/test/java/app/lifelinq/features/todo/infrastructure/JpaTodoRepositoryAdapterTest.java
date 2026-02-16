package app.lifelinq.features.todo.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = TodoJpaTestApplication.class)
@ActiveProfiles("test")
class JpaTodoRepositoryAdapterTest {

    @Autowired
    private TodoJpaRepository todoJpaRepository;

    @Test
    void savesAndLoadsRoundTrip() {
        JpaTodoRepositoryAdapter adapter = new JpaTodoRepositoryAdapter(todoJpaRepository, new TodoMapper());
        Todo todo = new Todo(UUID.randomUUID(), UUID.randomUUID(), "Write tests");

        adapter.save(todo);
        Optional<Todo> loaded = adapter.findById(todo.getId());

        assertTrue(loaded.isPresent());
        assertEquals(todo.getId(), loaded.get().getId());
        assertEquals(todo.getHouseholdId(), loaded.get().getHouseholdId());
        assertEquals(todo.getText(), loaded.get().getText());
        assertEquals(TodoStatus.OPEN, loaded.get().getStatus());

        loaded.get().toggle(Instant.parse("2026-01-01T00:00:00Z"));
        assertEquals(TodoStatus.COMPLETED, loaded.get().getStatus());
    }

    @Test
    void findsByHouseholdAndMonthOrderedByDueDateThenId() {
        JpaTodoRepositoryAdapter adapter = new JpaTodoRepositoryAdapter(todoJpaRepository, new TodoMapper());
        UUID householdId = UUID.randomUUID();
        UUID otherHousehold = UUID.randomUUID();

        Todo febLater = Todo.rehydrate(
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                householdId,
                "Later",
                TodoStatus.OPEN,
                LocalDate.of(2026, 2, 20),
                null,
                null
        );
        Todo febEarlierHigherId = Todo.rehydrate(
                UUID.fromString("00000000-0000-0000-0000-000000000003"),
                householdId,
                "Early B",
                TodoStatus.OPEN,
                LocalDate.of(2026, 2, 10),
                null,
                null
        );
        Todo febEarlierLowerId = Todo.rehydrate(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                householdId,
                "Early A",
                TodoStatus.OPEN,
                LocalDate.of(2026, 2, 10),
                null,
                null
        );
        Todo outOfMonth = Todo.rehydrate(
                UUID.randomUUID(),
                householdId,
                "March",
                TodoStatus.OPEN,
                LocalDate.of(2026, 3, 1),
                null,
                null
        );
        Todo otherHouseholdTodo = Todo.rehydrate(
                UUID.randomUUID(),
                otherHousehold,
                "Other",
                TodoStatus.OPEN,
                LocalDate.of(2026, 2, 12),
                null,
                null
        );
        Todo withoutDueDate = Todo.rehydrate(
                UUID.randomUUID(),
                householdId,
                "No due",
                TodoStatus.OPEN,
                null,
                null,
                null
        );
        Todo deleted = Todo.rehydrate(
                UUID.randomUUID(),
                householdId,
                "Deleted",
                TodoStatus.OPEN,
                LocalDate.of(2026, 2, 15),
                null,
                Instant.parse("2026-02-16T00:00:00Z")
        );

        adapter.save(febLater);
        adapter.save(febEarlierHigherId);
        adapter.save(febEarlierLowerId);
        adapter.save(outOfMonth);
        adapter.save(otherHouseholdTodo);
        adapter.save(withoutDueDate);
        adapter.save(deleted);

        List<Todo> result = adapter.findByHouseholdIdAndDueDateBetween(
                householdId,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28)
        );

        assertEquals(3, result.size());
        assertEquals(febEarlierLowerId.getId(), result.get(0).getId());
        assertEquals(febEarlierHigherId.getId(), result.get(1).getId());
        assertEquals(febLater.getId(), result.get(2).getId());
    }
}
