package app.lifelinq.features.todo.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoScope;
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
        assertEquals(todo.getGroupId(), loaded.get().getGroupId());
        assertEquals(todo.getText(), loaded.get().getText());
        assertEquals(TodoStatus.OPEN, loaded.get().getStatus());

        loaded.get().toggle(Instant.parse("2026-01-01T00:00:00Z"));
        assertEquals(TodoStatus.COMPLETED, loaded.get().getStatus());
    }

    @Test
    void findsByGroupAndMonthOrderedByDueDateThenId() {
        JpaTodoRepositoryAdapter adapter = new JpaTodoRepositoryAdapter(todoJpaRepository, new TodoMapper());
        UUID groupId = UUID.randomUUID();
        UUID otherGroup = UUID.randomUUID();

        Todo febLater = Todo.rehydrate(
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                groupId,
                "Later",
                TodoStatus.OPEN,
                LocalDate.of(2026, 2, 20),
                null,
                null
        );
        Todo febEarlierHigherId = Todo.rehydrate(
                UUID.fromString("00000000-0000-0000-0000-000000000003"),
                groupId,
                "Early B",
                TodoStatus.OPEN,
                LocalDate.of(2026, 2, 10),
                null,
                null
        );
        Todo febEarlierLowerId = Todo.rehydrate(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                groupId,
                "Early A",
                TodoStatus.OPEN,
                LocalDate.of(2026, 2, 10),
                null,
                null
        );
        Todo outOfMonth = Todo.rehydrate(
                UUID.randomUUID(),
                groupId,
                "March",
                TodoStatus.OPEN,
                LocalDate.of(2026, 3, 1),
                null,
                null
        );
        Todo otherGroupTodo = Todo.rehydrate(
                UUID.randomUUID(),
                otherGroup,
                "Other",
                TodoStatus.OPEN,
                LocalDate.of(2026, 2, 12),
                null,
                null
        );
        Todo withoutDueDate = Todo.rehydrate(
                UUID.randomUUID(),
                groupId,
                "No due",
                TodoStatus.OPEN,
                null,
                null,
                null
        );
        Todo monthGoal = Todo.rehydrate(
                UUID.randomUUID(),
                groupId,
                "Month goal",
                TodoStatus.OPEN,
                TodoScope.MONTH,
                null,
                null,
                2026,
                null,
                2,
                null,
                Instant.parse("2026-02-01T00:00:00Z"),
                null
        );
        Todo deleted = Todo.rehydrate(
                UUID.randomUUID(),
                groupId,
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
        adapter.save(otherGroupTodo);
        adapter.save(withoutDueDate);
        adapter.save(deleted);
        adapter.save(monthGoal);

        List<Todo> result = adapter.listForMonth(
                groupId,
                2026,
                2,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28)
        );

        assertEquals(4, result.size());
        assertEquals(febEarlierLowerId.getId(), result.get(0).getId());
        assertEquals(febEarlierHigherId.getId(), result.get(1).getId());
        assertEquals(febLater.getId(), result.get(2).getId());
        assertEquals(monthGoal.getId(), result.get(3).getId());
    }

    @Test
    void listByGroupFiltersDeletedAndSortsStably() {
        JpaTodoRepositoryAdapter adapter = new JpaTodoRepositoryAdapter(todoJpaRepository, new TodoMapper());
        UUID groupId = UUID.randomUUID();

        Todo later = Todo.rehydrate(
                UUID.fromString("00000000-0000-0000-0000-000000000021"),
                groupId,
                "Later",
                TodoStatus.OPEN,
                TodoScope.LATER,
                null,
                null,
                null,
                null,
                null,
                null,
                Instant.parse("2026-02-05T00:00:00Z"),
                null
        );
        Todo dayWithTime = Todo.rehydrate(
                UUID.fromString("00000000-0000-0000-0000-000000000022"),
                groupId,
                "Day with time",
                TodoStatus.OPEN,
                TodoScope.DAY,
                LocalDate.of(2026, 2, 10),
                java.time.LocalTime.of(8, 0),
                null,
                null,
                null,
                null,
                Instant.parse("2026-02-01T00:00:00Z"),
                null
        );
        Todo dayWithoutTime = Todo.rehydrate(
                UUID.fromString("00000000-0000-0000-0000-000000000023"),
                groupId,
                "Day no time",
                TodoStatus.OPEN,
                TodoScope.DAY,
                LocalDate.of(2026, 2, 10),
                null,
                null,
                null,
                null,
                null,
                Instant.parse("2026-02-01T00:00:00Z"),
                null
        );
        Todo deleted = Todo.rehydrate(
                UUID.randomUUID(),
                groupId,
                "Deleted",
                TodoStatus.OPEN,
                TodoScope.LATER,
                null,
                null,
                null,
                null,
                null,
                null,
                Instant.parse("2026-02-01T00:00:00Z"),
                Instant.parse("2026-02-10T00:00:00Z")
        );

        adapter.save(later);
        adapter.save(dayWithoutTime);
        adapter.save(dayWithTime);
        adapter.save(deleted);

        List<Todo> result = adapter.listByGroup(groupId, TodoStatus.ALL);

        assertEquals(3, result.size());
        assertEquals(dayWithTime.getId(), result.get(0).getId());
        assertEquals(dayWithoutTime.getId(), result.get(1).getId());
        assertEquals(later.getId(), result.get(2).getId());
    }

    @Test
    void listByGroupNormalizesLegacySchedulingRows() {
        JpaTodoRepositoryAdapter adapter = new JpaTodoRepositoryAdapter(todoJpaRepository, new TodoMapper());
        UUID groupId = UUID.randomUUID();
        UUID legacyDayId = UUID.randomUUID();
        UUID legacyMonthId = UUID.randomUUID();

        todoJpaRepository.save(new TodoEntity(
                legacyDayId,
                groupId,
                "Legacy due date row",
                TodoStatus.OPEN,
                TodoScope.LATER,
                LocalDate.of(2026, 4, 6),
                java.time.LocalTime.of(18, 0),
                null,
                null,
                null,
                null,
                Instant.parse("2026-04-01T00:00:00Z"),
                null
        ));
        todoJpaRepository.save(new TodoEntity(
                legacyMonthId,
                groupId,
                "Legacy month row",
                TodoStatus.OPEN,
                TodoScope.DAY,
                null,
                null,
                2026,
                null,
                4,
                null,
                Instant.parse("2026-04-02T00:00:00Z"),
                null
        ));

        List<Todo> result = adapter.listByGroup(groupId, TodoStatus.ALL);

        assertEquals(2, result.size());
        Todo legacyDay = result.stream().filter(todo -> todo.getId().equals(legacyDayId)).findFirst().orElseThrow();
        assertEquals(TodoScope.DAY, legacyDay.getScope());
        assertEquals(LocalDate.of(2026, 4, 6), legacyDay.getDueDate());
        assertEquals(java.time.LocalTime.of(18, 0), legacyDay.getDueTime());

        Todo legacyMonth = result.stream().filter(todo -> todo.getId().equals(legacyMonthId)).findFirst().orElseThrow();
        assertEquals(TodoScope.MONTH, legacyMonth.getScope());
        assertEquals(Integer.valueOf(2026), legacyMonth.getScopeYear());
        assertEquals(Integer.valueOf(4), legacyMonth.getScopeMonth());
    }
}
