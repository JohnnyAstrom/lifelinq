package app.lifelinq.features.todo.application;

import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoRepository;
import app.lifelinq.features.todo.domain.TodoScope;
import app.lifelinq.features.todo.domain.TodoStatus;
import app.lifelinq.features.group.contract.EnsureGroupMemberUseCase;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
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
    private final TodoRepository todoRepository;
    private final EnsureGroupMemberUseCase ensureGroupMemberUseCase;

    public TodoApplicationService(
            TodoRepository todoRepository,
            EnsureGroupMemberUseCase ensureGroupMemberUseCase
    ) {
        if (todoRepository == null) {
            throw new IllegalArgumentException("todoRepository must not be null");
        }
        if (ensureGroupMemberUseCase == null) {
            throw new IllegalArgumentException("ensureGroupMemberUseCase must not be null");
        }
        this.todoRepository = todoRepository;
        this.createTodoUseCase = new CreateTodoUseCase(todoRepository);
        this.completeTodoUseCase = new CompleteTodoUseCase(todoRepository);
        this.deleteTodoUseCase = new DeleteTodoUseCase(todoRepository);
        this.listTodosUseCase = new ListTodosUseCase(todoRepository);
        this.listTodosForMonthUseCase = new ListTodosForMonthUseCase(todoRepository);
        this.updateTodoUseCase = new UpdateTodoUseCase(todoRepository);
        this.ensureGroupMemberUseCase = ensureGroupMemberUseCase;
    }

    @Transactional
    public UUID createTodo(
            UUID groupId,
            UUID actorUserId,
            String text,
            TodoScope scope,
            LocalDate dueDate,
            LocalTime dueTime,
            Integer scopeYear,
            Integer scopeWeek,
            Integer scopeMonth
    ) {
        ensureGroupMemberUseCase.execute(groupId, actorUserId);
        CreateTodoResult result = createTodoUseCase.execute(
                new CreateTodoCommand(groupId, text, scope, dueDate, dueTime, scopeYear, scopeWeek, scopeMonth)
        );
        return result.getTodoId();
    }

    @Transactional
    public boolean completeTodo(UUID todoId, UUID actorUserId, Instant now) {
        UUID groupId = findTodoGroupId(todoId);
        if (groupId == null) {
            return false;
        }
        ensureGroupMemberUseCase.execute(groupId, actorUserId);
        CompleteTodoResult result = completeTodoUseCase.execute(new CompleteTodoCommand(todoId, now));
        return result.isCompleted();
    }

    @Transactional
    public boolean updateTodo(
            UUID todoId,
            UUID actorUserId,
            String text,
            TodoScope scope,
            LocalDate dueDate,
            LocalTime dueTime,
            Integer scopeYear,
            Integer scopeWeek,
            Integer scopeMonth
    ) {
        UUID groupId = findTodoGroupId(todoId);
        if (groupId == null) {
            return false;
        }
        ensureGroupMemberUseCase.execute(groupId, actorUserId);
        UpdateTodoResult result = updateTodoUseCase.execute(
                new UpdateTodoCommand(todoId, text, scope, dueDate, dueTime, scopeYear, scopeWeek, scopeMonth)
        );
        return result.isUpdated();
    }

    @Transactional
    public boolean deleteTodo(UUID todoId, UUID actorUserId, Instant now) {
        UUID groupId = findTodoGroupId(todoId);
        if (groupId == null) {
            return false;
        }
        ensureGroupMemberUseCase.execute(groupId, actorUserId);
        DeleteTodoResult result = deleteTodoUseCase.execute(new DeleteTodoCommand(todoId, now));
        return result.isDeleted();
    }

    @Transactional(readOnly = true)
    public List<Todo> listTodos(UUID groupId, TodoStatus status) {
        ListTodosResult result = listTodosUseCase.execute(new TodoQuery(groupId, status));
        return result.getTodos();
    }

    @Transactional(readOnly = true)
    public List<Todo> listTodosForMonth(UUID groupId, int year, int month) {
        ListTodosResult result = listTodosForMonthUseCase.execute(new TodoMonthQuery(groupId, year, month));
        return result.getTodos();
    }

    private UUID findTodoGroupId(UUID todoId) {
        if (todoId == null) {
            throw new IllegalArgumentException("todoId must not be null");
        }
        return todoRepository.findById(todoId)
                .map(Todo::getGroupId)
                .orElse(null);
    }
}
