package app.lifelinq.features.todo.api;

import app.lifelinq.features.todo.application.CompleteTodoCommand;
import app.lifelinq.features.todo.application.CompleteTodoResult;
import app.lifelinq.features.todo.application.CompleteTodoUseCase;
import app.lifelinq.features.todo.application.CreateTodoCommand;
import app.lifelinq.features.todo.application.CreateTodoResult;
import app.lifelinq.features.todo.application.CreateTodoUseCase;
import app.lifelinq.features.todo.application.ListTodosResult;
import app.lifelinq.features.todo.application.ListTodosUseCase;
import app.lifelinq.features.todo.application.TodoQuery;
import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TodoController {
    private final CreateTodoUseCase createTodoUseCase;
    private final CompleteTodoUseCase completeTodoUseCase;
    private final ListTodosUseCase listTodosUseCase;

    public TodoController(
            CreateTodoUseCase createTodoUseCase,
            CompleteTodoUseCase completeTodoUseCase,
            ListTodosUseCase listTodosUseCase
    ) {
        this.createTodoUseCase = createTodoUseCase;
        this.completeTodoUseCase = completeTodoUseCase;
        this.listTodosUseCase = listTodosUseCase;
    }

    @PostMapping("/todos")
    public CreateTodoResponse create(@RequestBody CreateTodoRequest request) {
        CreateTodoCommand command = new CreateTodoCommand(request.getHouseholdId(), request.getText());
        CreateTodoResult result = createTodoUseCase.execute(command);
        return new CreateTodoResponse(result.getTodoId());
    }

    @PostMapping("/todos/{id}/complete")
    public CompleteTodoResponse complete(@PathVariable("id") UUID id) {
        CompleteTodoCommand command = new CompleteTodoCommand(id, Instant.now());
        CompleteTodoResult result = completeTodoUseCase.execute(command);
        return new CompleteTodoResponse(result.isCompleted());
    }

    @GetMapping("/todos")
    public ListTodosResponse list(
            @RequestParam(name = "status", defaultValue = "ALL") String status,
            @RequestParam(name = "householdId", required = false) UUID householdId
    ) {
        TodoStatus filter = TodoStatus.valueOf(status);
        ListTodosResult result = listTodosUseCase.execute(new TodoQuery(householdId, filter));
        return new ListTodosResponse(toResponseItems(result.getTodos()));
    }

    private List<TodoItemResponse> toResponseItems(List<Todo> todos) {
        List<TodoItemResponse> items = new ArrayList<>();
        for (Todo todo : todos) {
            items.add(new TodoItemResponse(
                    todo.getId(),
                    todo.getHouseholdId(),
                    todo.getText(),
                    todo.getStatus()
            ));
        }
        return items;
    }
}
