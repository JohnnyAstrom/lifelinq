package app.lifelinq.features.todo.application;

import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoRepository;
import app.lifelinq.features.todo.domain.TodoStatus;
import app.lifelinq.features.user.application.UserApplicationService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class TodoApplicationService {
    private final CreateTodoUseCase createTodoUseCase;
    private final CompleteTodoUseCase completeTodoUseCase;
    private final DeleteTodoUseCase deleteTodoUseCase;
    private final ListTodosUseCase listTodosUseCase;
    private final ListTodosForMonthUseCase listTodosForMonthUseCase;
    private final UpdateTodoUseCase updateTodoUseCase;
    private final UserApplicationService userApplicationService;

    public TodoApplicationService(
            TodoRepository todoRepository,
            UserApplicationService userApplicationService
    ) {
        if (todoRepository == null) {
            throw new IllegalArgumentException("todoRepository must not be null");
        }
        if (userApplicationService == null) {
            throw new IllegalArgumentException("userApplicationService must not be null");
        }
        this.createTodoUseCase = new CreateTodoUseCase(todoRepository);
        this.completeTodoUseCase = new CompleteTodoUseCase(todoRepository);
        this.deleteTodoUseCase = new DeleteTodoUseCase(todoRepository);
        this.listTodosUseCase = new ListTodosUseCase(todoRepository);
        this.listTodosForMonthUseCase = new ListTodosForMonthUseCase(todoRepository);
        this.updateTodoUseCase = new UpdateTodoUseCase(todoRepository);
        this.userApplicationService = userApplicationService;
    }

    @Transactional
    public UUID createTodo(
            UUID householdId,
            UUID actorUserId,
            String text,
            java.time.LocalDate dueDate,
            java.time.LocalTime dueTime
    ) {
        userApplicationService.ensureUserExists(actorUserId);
        CreateTodoResult result = createTodoUseCase.execute(
                new CreateTodoCommand(householdId, text, dueDate, dueTime)
        );
        return result.getTodoId();
    }

    @Transactional
    public boolean completeTodo(UUID todoId, UUID actorUserId, Instant now) {
        userApplicationService.ensureUserExists(actorUserId);
        CompleteTodoResult result = completeTodoUseCase.execute(new CompleteTodoCommand(todoId, now));
        return result.isCompleted();
    }

    @Transactional
    public boolean updateTodo(
            UUID todoId,
            UUID actorUserId,
            String text,
            java.time.LocalDate dueDate,
            java.time.LocalTime dueTime
    ) {
        userApplicationService.ensureUserExists(actorUserId);
        UpdateTodoResult result = updateTodoUseCase.execute(new UpdateTodoCommand(todoId, text, dueDate, dueTime));
        return result.isUpdated();
    }

    @Transactional
    public boolean deleteTodo(UUID todoId, UUID actorUserId, Instant now) {
        userApplicationService.ensureUserExists(actorUserId);
        DeleteTodoResult result = deleteTodoUseCase.execute(new DeleteTodoCommand(todoId, now));
        return result.isDeleted();
    }

    @Transactional(readOnly = true)
    public List<Todo> listTodos(UUID householdId, TodoStatus status) {
        ListTodosResult result = listTodosUseCase.execute(new TodoQuery(householdId, status));
        return result.getTodos();
    }

    @Transactional(readOnly = true)
    public List<Todo> listTodosForMonth(UUID householdId, int year, int month) {
        ListTodosResult result = listTodosForMonthUseCase.execute(new TodoMonthQuery(householdId, year, month));
        return result.getTodos();
    }
}
