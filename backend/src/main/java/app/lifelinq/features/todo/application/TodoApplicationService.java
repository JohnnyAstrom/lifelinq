package app.lifelinq.features.todo.application;

import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoRepository;
import app.lifelinq.features.todo.domain.TodoStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class TodoApplicationService {
    private final CreateTodoUseCase createTodoUseCase;
    private final CompleteTodoUseCase completeTodoUseCase;
    private final ListTodosUseCase listTodosUseCase;

    public TodoApplicationService(TodoRepository todoRepository) {
        if (todoRepository == null) {
            throw new IllegalArgumentException("todoRepository must not be null");
        }
        this.createTodoUseCase = new CreateTodoUseCase();
        this.completeTodoUseCase = new CompleteTodoUseCase(todoRepository);
        this.listTodosUseCase = new ListTodosUseCase(todoRepository);
    }

    @Transactional
    public UUID createTodo(UUID householdId, String text) {
        CreateTodoResult result = createTodoUseCase.execute(new CreateTodoCommand(householdId, text));
        return result.getTodoId();
    }

    @Transactional
    public boolean completeTodo(UUID todoId, Instant now) {
        CompleteTodoResult result = completeTodoUseCase.execute(new CompleteTodoCommand(todoId, now));
        return result.isCompleted();
    }

    @Transactional(readOnly = true)
    public List<Todo> listTodos(UUID householdId, TodoStatus status) {
        ListTodosResult result = listTodosUseCase.execute(new TodoQuery(householdId, status));
        return result.getTodos();
    }
}
