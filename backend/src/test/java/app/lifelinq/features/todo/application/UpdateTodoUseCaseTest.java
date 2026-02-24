package app.lifelinq.features.todo.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoRepository;
import app.lifelinq.features.todo.domain.TodoScope;
import app.lifelinq.features.todo.domain.TodoStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UpdateTodoUseCaseTest {

    @Test
    void updatesTodoTextAndSchedule() {
        InMemoryTodoRepository todoRepository = new InMemoryTodoRepository();
        UUID householdId = UUID.randomUUID();
        Todo original = new Todo(UUID.randomUUID(), householdId, "Buy milk");
        todoRepository.save(original);
        UpdateTodoUseCase useCase = new UpdateTodoUseCase(todoRepository);
        UpdateTodoCommand command = new UpdateTodoCommand(
                original.getId(),
                "Buy oat milk",
                LocalDate.of(2026, 2, 14),
                LocalTime.of(9, 30)
        );

        UpdateTodoResult result = useCase.execute(command);

        assertTrue(result.isUpdated());
        Todo updated = todoRepository.findById(original.getId()).orElseThrow();
        assertEquals("Buy oat milk", updated.getText());
        assertEquals(LocalDate.of(2026, 2, 14), updated.getDueDate());
        assertEquals(LocalTime.of(9, 30), updated.getDueTime());
    }

    @Test
    void preservesCompletedStatusWhenUpdating() {
        InMemoryTodoRepository todoRepository = new InMemoryTodoRepository();
        UUID householdId = UUID.randomUUID();
        Todo original = new Todo(UUID.randomUUID(), householdId, "Pay bill");
        original.toggle(Instant.now());
        todoRepository.save(original);
        UpdateTodoUseCase useCase = new UpdateTodoUseCase(todoRepository);
        UpdateTodoCommand command = new UpdateTodoCommand(original.getId(), "Pay electricity bill", null, null);

        UpdateTodoResult result = useCase.execute(command);

        assertTrue(result.isUpdated());
        Todo updated = todoRepository.findById(original.getId()).orElseThrow();
        assertEquals(TodoStatus.COMPLETED, updated.getStatus());
    }

    @Test
    void returnsFalseWhenTodoDoesNotExist() {
        UpdateTodoUseCase useCase = new UpdateTodoUseCase(new InMemoryTodoRepository());
        UpdateTodoCommand command = new UpdateTodoCommand(UUID.randomUUID(), "Task", null, null);

        UpdateTodoResult result = useCase.execute(command);

        assertFalse(result.isUpdated());
    }

    @Test
    void canChangeScopeFromLaterToWeek() {
        InMemoryTodoRepository todoRepository = new InMemoryTodoRepository();
        Todo original = new Todo(UUID.randomUUID(), UUID.randomUUID(), "Plan grocery prep");
        todoRepository.save(original);
        UpdateTodoUseCase useCase = new UpdateTodoUseCase(todoRepository);

        UpdateTodoResult result = useCase.execute(new UpdateTodoCommand(
                original.getId(),
                "Plan grocery prep",
                TodoScope.WEEK,
                null,
                null,
                2026,
                10,
                null
        ));

        assertTrue(result.isUpdated());
        Todo updated = todoRepository.findById(original.getId()).orElseThrow();
        assertEquals(TodoScope.WEEK, updated.getScope());
        assertEquals(2026, updated.getScopeYear());
        assertEquals(10, updated.getScopeWeek());
        assertEquals(null, updated.getDueDate());
    }

    @Test
    void requiresCommand() {
        UpdateTodoUseCase useCase = new UpdateTodoUseCase(new InMemoryTodoRepository());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    @Test
    void rejectsDueTimeWithoutDueDate() {
        InMemoryTodoRepository todoRepository = new InMemoryTodoRepository();
        Todo original = new Todo(UUID.randomUUID(), UUID.randomUUID(), "Task");
        todoRepository.save(original);
        UpdateTodoUseCase useCase = new UpdateTodoUseCase(todoRepository);
        UpdateTodoCommand command = new UpdateTodoCommand(
                original.getId(),
                "Task",
                TodoScope.DAY,
                null,
                LocalTime.of(9, 0),
                null,
                null,
                null
        );
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }

    private static final class InMemoryTodoRepository implements TodoRepository {
        private final List<Todo> saved = new ArrayList<>();

        @Override
        public java.util.Optional<Todo> findById(UUID id) {
            for (Todo todo : saved) {
                if (id.equals(todo.getId())) {
                    return java.util.Optional.of(todo);
                }
            }
            return java.util.Optional.empty();
        }

        @Override
        public void save(Todo todo) {
            saved.removeIf(existing -> existing.getId().equals(todo.getId()));
            saved.add(todo);
        }

        @Override
        public List<Todo> listByHousehold(UUID householdId, TodoStatus statusFilter) {
            return new ArrayList<>(saved);
        }

        @Override
        public List<Todo> listForMonth(UUID householdId, int year, int month, LocalDate startDate, LocalDate endDate) {
            return new ArrayList<>();
        }
    }
}
