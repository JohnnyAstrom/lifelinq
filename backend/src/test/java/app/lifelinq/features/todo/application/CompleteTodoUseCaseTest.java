package app.lifelinq.features.todo.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoRepository;
import app.lifelinq.features.todo.domain.TodoStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CompleteTodoUseCaseTest {

    @Test
    void completesWhenFound() {
        InMemoryTodoRepository repository = new InMemoryTodoRepository();
        Todo todo = new Todo(UUID.randomUUID(), UUID.randomUUID(), "Task");
        repository.save(todo);
        int initialSaveCount = repository.saveCount;

        CompleteTodoUseCase useCase = new CompleteTodoUseCase(repository);
        CompleteTodoCommand command = new CompleteTodoCommand(todo.getId(), Instant.parse("2026-01-01T00:00:00Z"));

        CompleteTodoResult result = useCase.execute(command);

        assertEquals(true, result.isCompleted());
        assertEquals(initialSaveCount + 1, repository.saveCount);
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), repository.findById(todo.getId()).orElseThrow().getCompletedAt());
    }

    @Test
    void togglesBackToOpenWhenAlreadyCompleted() {
        InMemoryTodoRepository repository = new InMemoryTodoRepository();
        Todo todo = new Todo(UUID.randomUUID(), UUID.randomUUID(), "Task");
        repository.save(todo);
        CompleteTodoUseCase useCase = new CompleteTodoUseCase(repository);

        useCase.execute(new CompleteTodoCommand(todo.getId(), Instant.parse("2026-01-01T00:00:00Z")));
        int saveCountAfterComplete = repository.saveCount;

        CompleteTodoResult result = useCase.execute(new CompleteTodoCommand(todo.getId(), Instant.parse("2026-01-02T00:00:00Z")));

        assertEquals(false, result.isCompleted());
        assertEquals(saveCountAfterComplete + 1, repository.saveCount);
        assertEquals(null, repository.findById(todo.getId()).orElseThrow().getCompletedAt());
    }

    @Test
    void returnsFalseWhenNotFound() {
        CompleteTodoUseCase useCase = new CompleteTodoUseCase(new InMemoryTodoRepository());
        CompleteTodoCommand command = new CompleteTodoCommand(UUID.randomUUID(), Instant.parse("2026-01-01T00:00:00Z"));

        CompleteTodoResult result = useCase.execute(command);

        assertEquals(false, result.isCompleted());
    }

    @Test
    void requiresCommand() {
        CompleteTodoUseCase useCase = new CompleteTodoUseCase(new InMemoryTodoRepository());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    @Test
    void requiresNow() {
        CompleteTodoUseCase useCase = new CompleteTodoUseCase(new InMemoryTodoRepository());
        CompleteTodoCommand command = new CompleteTodoCommand(UUID.randomUUID(), null);
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }

    private static final class InMemoryTodoRepository implements TodoRepository {
        private final Map<UUID, Todo> store = new HashMap<>();
        private int saveCount = 0;

        @Override
        public Optional<Todo> findById(UUID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public void save(Todo todo) {
            store.put(todo.getId(), todo);
            saveCount++;
        }

        @Override
        public java.util.List<Todo> listByHousehold(UUID householdId, TodoStatus statusFilter) {
            return new java.util.ArrayList<>(store.values());
        }

        @Override
        public java.util.List<Todo> listForMonth(UUID householdId, int year, int month, LocalDate startDate, LocalDate endDate) {
            return new java.util.ArrayList<>();
        }
    }
}
