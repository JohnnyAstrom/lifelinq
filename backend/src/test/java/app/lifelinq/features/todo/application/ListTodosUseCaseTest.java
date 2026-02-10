package app.lifelinq.features.todo.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoRepository;
import app.lifelinq.features.todo.domain.TodoStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ListTodosUseCaseTest {

    @Test
    void filtersByStatus() {
        InMemoryTodoRepository repository = new InMemoryTodoRepository();
        Todo open = new Todo(UUID.randomUUID(), UUID.randomUUID(), "Open");
        Todo completed = new Todo(UUID.randomUUID(), UUID.randomUUID(), "Done");
        completed.complete(java.time.Instant.parse("2026-01-01T00:00:00Z"));
        repository.save(open);
        repository.save(completed);

        ListTodosUseCase useCase = new ListTodosUseCase(repository);

        ListTodosResult openResult = useCase.execute(new TodoQuery(null, TodoStatus.OPEN));
        ListTodosResult completedResult = useCase.execute(new TodoQuery(null, TodoStatus.COMPLETED));
        ListTodosResult allResult = useCase.execute(new TodoQuery(null, TodoStatus.ALL));

        assertEquals(1, openResult.getTodos().size());
        assertEquals(1, completedResult.getTodos().size());
        assertEquals(2, allResult.getTodos().size());
    }

    @Test
    void filtersByHouseholdWhenProvided() {
        InMemoryTodoRepository repository = new InMemoryTodoRepository();
        UUID householdId = UUID.randomUUID();
        repository.save(new Todo(UUID.randomUUID(), householdId, "A"));
        repository.save(new Todo(UUID.randomUUID(), UUID.randomUUID(), "B"));

        ListTodosUseCase useCase = new ListTodosUseCase(repository);
        ListTodosResult result = useCase.execute(new TodoQuery(householdId, TodoStatus.ALL));

        assertEquals(1, result.getTodos().size());
        assertEquals(householdId, result.getTodos().get(0).getHouseholdId());
    }

    @Test
    void requiresQuery() {
        ListTodosUseCase useCase = new ListTodosUseCase(new InMemoryTodoRepository());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    @Test
    void requiresStatusFilter() {
        ListTodosUseCase useCase = new ListTodosUseCase(new InMemoryTodoRepository());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(new TodoQuery(null, null)));
    }

    private static final class InMemoryTodoRepository implements TodoRepository {
        private final List<Todo> store = new ArrayList<>();

        @Override
        public Optional<Todo> findById(UUID id) {
            for (Todo todo : store) {
                if (id.equals(todo.getId())) {
                    return Optional.of(todo);
                }
            }
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
    }
}
