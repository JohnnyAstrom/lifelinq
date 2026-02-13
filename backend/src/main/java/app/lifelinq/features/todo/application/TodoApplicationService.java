package app.lifelinq.features.todo.application;

import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoRepository;
import app.lifelinq.features.todo.domain.TodoStatus;
import app.lifelinq.features.user.application.EnsureUserExistsUseCase;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class TodoApplicationService {
    private final CreateTodoUseCase createTodoUseCase;
    private final CompleteTodoUseCase completeTodoUseCase;
    private final ListTodosUseCase listTodosUseCase;
    private final EnsureUserExistsUseCase ensureUserExistsUseCase;

    public TodoApplicationService(
            TodoRepository todoRepository,
            EnsureUserExistsUseCase ensureUserExistsUseCase
    ) {
        if (todoRepository == null) {
            throw new IllegalArgumentException("todoRepository must not be null");
        }
        if (ensureUserExistsUseCase == null) {
            throw new IllegalArgumentException("ensureUserExistsUseCase must not be null");
        }
        this.createTodoUseCase = new CreateTodoUseCase(todoRepository);
        this.completeTodoUseCase = new CompleteTodoUseCase(todoRepository);
        this.listTodosUseCase = new ListTodosUseCase(todoRepository);
        this.ensureUserExistsUseCase = ensureUserExistsUseCase;
    }

    @Transactional
    public UUID createTodo(
            UUID householdId,
            UUID actorUserId,
            String text,
            java.time.LocalDate dueDate,
            java.time.LocalTime dueTime
    ) {
        ensureUserExistsUseCase.execute(actorUserId);
        CreateTodoResult result = createTodoUseCase.execute(
                new CreateTodoCommand(householdId, text, dueDate, dueTime)
        );
        return result.getTodoId();
    }

    @Transactional
    public boolean completeTodo(UUID todoId, UUID actorUserId, Instant now) {
        ensureUserExistsUseCase.execute(actorUserId);
        CompleteTodoResult result = completeTodoUseCase.execute(new CompleteTodoCommand(todoId, now));
        return result.isCompleted();
    }

    @Transactional(readOnly = true)
    public List<Todo> listTodos(UUID householdId, TodoStatus status) {
        ListTodosResult result = listTodosUseCase.execute(new TodoQuery(householdId, status));
        return result.getTodos();
    }
}
