package app.lifelinq.features.todo.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoRepository;
import app.lifelinq.features.todo.domain.TodoScope;
import app.lifelinq.features.todo.domain.TodoStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ListTodosForMonthUseCaseTest {

    @Test
    void returns_only_requested_month_in_due_date_order_then_id() {
        UUID groupId = UUID.randomUUID();
        InMemoryTodoRepository repository = new InMemoryTodoRepository();
        Todo outOfMonth = new Todo(UUID.randomUUID(), groupId, "March", LocalDate.of(2026, 3, 1), null);
        Todo febLater = new Todo(UUID.fromString("00000000-0000-0000-0000-000000000002"), groupId, "B", LocalDate.of(2026, 2, 20), null);
        Todo febEarlierHigherId = new Todo(UUID.fromString("00000000-0000-0000-0000-000000000003"), groupId, "C", LocalDate.of(2026, 2, 10), null);
        Todo febEarlierLowerId = new Todo(UUID.fromString("00000000-0000-0000-0000-000000000001"), groupId, "A", LocalDate.of(2026, 2, 10), null);
        Todo noDueDate = new Todo(UUID.randomUUID(), groupId, "No due", null, null);
        Todo monthGoal = new Todo(
                UUID.randomUUID(),
                groupId,
                "Month goal",
                TodoScope.MONTH,
                null,
                null,
                2026,
                null,
                2,
                Instant.parse("2026-02-01T00:00:00Z")
        );
        repository.save(outOfMonth);
        repository.save(febLater);
        repository.save(febEarlierHigherId);
        repository.save(febEarlierLowerId);
        repository.save(noDueDate);
        repository.save(monthGoal);

        ListTodosForMonthUseCase useCase = new ListTodosForMonthUseCase(repository);
        List<Todo> items = useCase.execute(new TodoMonthQuery(groupId, 2026, 2)).getTodos();

        assertThat(items).extracting(Todo::getId).containsExactly(
                febEarlierLowerId.getId(),
                febEarlierHigherId.getId(),
                febLater.getId(),
                monthGoal.getId()
        );
    }

    @Test
    void rejects_invalid_month() {
        ListTodosForMonthUseCase useCase = new ListTodosForMonthUseCase(new InMemoryTodoRepository());

        assertThatThrownBy(() -> useCase.execute(new TodoMonthQuery(UUID.randomUUID(), 2026, 13)))
                .isInstanceOf(java.time.DateTimeException.class);
    }

    private static final class InMemoryTodoRepository implements TodoRepository {
        private final List<Todo> store = new ArrayList<>();

        @Override
        public Optional<Todo> findById(UUID id) {
            return Optional.empty();
        }

        @Override
        public void save(Todo todo) {
            store.add(todo);
        }

        @Override
        public List<Todo> listByGroup(UUID groupId, TodoStatus statusFilter) {
            return new ArrayList<>(store);
        }

        @Override
        public List<Todo> listForMonth(UUID groupId, int year, int month, LocalDate startDate, LocalDate endDate) {
            List<Todo> result = new ArrayList<>();
            for (Todo todo : store) {
                if (!groupId.equals(todo.getGroupId())) {
                    continue;
                }
                if (todo.getScope() == TodoScope.DAY && todo.getDueDate() != null
                        && (todo.getDueDate().isEqual(startDate) || todo.getDueDate().isAfter(startDate))
                        && (todo.getDueDate().isEqual(endDate) || todo.getDueDate().isBefore(endDate))) {
                    result.add(todo);
                }
                if (todo.getScope() == TodoScope.MONTH
                        && Integer.valueOf(year).equals(todo.getScopeYear())
                        && Integer.valueOf(month).equals(todo.getScopeMonth())) {
                    result.add(todo);
                }
            }
            result.sort(Comparator
                    .comparingInt((Todo todo) -> todo.getScope() == TodoScope.DAY ? 0 : 1)
                    .thenComparing(Todo::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Todo::getId));
            return result;
        }
    }
}
