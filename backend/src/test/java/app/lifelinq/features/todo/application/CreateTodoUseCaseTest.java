package app.lifelinq.features.todo.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoRepository;
import app.lifelinq.features.todo.domain.TodoScope;
import app.lifelinq.features.todo.domain.TodoStatus;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CreateTodoUseCaseTest {

    @Test
    void createsTodoAndReturnsId() {
        InMemoryTodoRepository todoRepository = new InMemoryTodoRepository();
        CreateTodoUseCase useCase = new CreateTodoUseCase(todoRepository);
        CreateTodoCommand command = new CreateTodoCommand(UUID.randomUUID(), "Buy milk");

        CreateTodoResult result = useCase.execute(command);

        assertNotNull(result);
        assertNotNull(result.getTodoId());
        assertEquals(1, todoRepository.saved.size());
    }

    @Test
    void requiresCommand() {
        CreateTodoUseCase useCase = new CreateTodoUseCase(new InMemoryTodoRepository());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    @Test
    void requiresGroupId() {
        CreateTodoUseCase useCase = new CreateTodoUseCase(new InMemoryTodoRepository());
        CreateTodoCommand command = new CreateTodoCommand(null, "Buy milk");
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }

    @Test
    void requiresNonBlankText() {
        CreateTodoUseCase useCase = new CreateTodoUseCase(new InMemoryTodoRepository());
        CreateTodoCommand command = new CreateTodoCommand(UUID.randomUUID(), " ");
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }

    @Test
    void createsWeekScopedTodo() {
        InMemoryTodoRepository todoRepository = new InMemoryTodoRepository();
        CreateTodoUseCase useCase = new CreateTodoUseCase(todoRepository);
        UUID groupId = UUID.randomUUID();

        CreateTodoResult result = useCase.execute(new CreateTodoCommand(
                groupId,
                "Week goal",
                TodoScope.WEEK,
                null,
                null,
                2026,
                9,
                null
        ));

        assertNotNull(result.getTodoId());
        Todo saved = todoRepository.findById(result.getTodoId()).orElseThrow();
        assertEquals(TodoScope.WEEK, saved.getScope());
        assertEquals(2026, saved.getScopeYear());
        assertEquals(9, saved.getScopeWeek());
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
            saved.add(todo);
        }

        @Override
        public List<Todo> listByGroup(UUID groupId, TodoStatus statusFilter) {
            return new ArrayList<>(saved);
        }

        @Override
        public List<Todo> listForMonth(UUID groupId, int year, int month, LocalDate startDate, LocalDate endDate) {
            return new ArrayList<>();
        }
    }
}
