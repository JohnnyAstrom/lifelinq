package app.lifelinq.features.todo.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoRepository;
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
        UUID householdId = UUID.randomUUID();
        InMemoryTodoRepository repository = new InMemoryTodoRepository();
        Todo outOfMonth = new Todo(UUID.randomUUID(), householdId, "March", LocalDate.of(2026, 3, 1), null);
        Todo febLater = new Todo(UUID.fromString("00000000-0000-0000-0000-000000000002"), householdId, "B", LocalDate.of(2026, 2, 20), null);
        Todo febEarlierHigherId = new Todo(UUID.fromString("00000000-0000-0000-0000-000000000003"), householdId, "C", LocalDate.of(2026, 2, 10), null);
        Todo febEarlierLowerId = new Todo(UUID.fromString("00000000-0000-0000-0000-000000000001"), householdId, "A", LocalDate.of(2026, 2, 10), null);
        Todo noDueDate = new Todo(UUID.randomUUID(), householdId, "No due", null, null);
        repository.save(outOfMonth);
        repository.save(febLater);
        repository.save(febEarlierHigherId);
        repository.save(febEarlierLowerId);
        repository.save(noDueDate);

        ListTodosForMonthUseCase useCase = new ListTodosForMonthUseCase(repository);
        List<Todo> items = useCase.execute(new TodoMonthQuery(householdId, 2026, 2)).getTodos();

        assertThat(items).extracting(Todo::getId).containsExactly(
                febEarlierLowerId.getId(),
                febEarlierHigherId.getId(),
                febLater.getId()
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
        public List<Todo> findAll() {
            return new ArrayList<>(store);
        }

        @Override
        public List<Todo> findByHouseholdIdAndDueDateBetween(UUID householdId, LocalDate startDate, LocalDate endDate) {
            List<Todo> result = new ArrayList<>();
            for (Todo todo : store) {
                if (!householdId.equals(todo.getHouseholdId()) || todo.getDueDate() == null) {
                    continue;
                }
                if ((todo.getDueDate().isEqual(startDate) || todo.getDueDate().isAfter(startDate))
                        && (todo.getDueDate().isEqual(endDate) || todo.getDueDate().isBefore(endDate))) {
                    result.add(todo);
                }
            }
            result.sort(Comparator.comparing(Todo::getDueDate).thenComparing(Todo::getId));
            return result;
        }
    }
}
