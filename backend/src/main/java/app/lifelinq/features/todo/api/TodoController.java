package app.lifelinq.features.todo.api;

import app.lifelinq.features.todo.application.TodoApplicationService;
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
    private final TodoApplicationService todoApplicationService;

    public TodoController(
            TodoApplicationService todoApplicationService
    ) {
        this.todoApplicationService = todoApplicationService;
    }

    @PostMapping("/todos")
    public CreateTodoResponse create(@RequestBody CreateTodoRequest request) {
        return new CreateTodoResponse(
                todoApplicationService.createTodo(request.getHouseholdId(), request.getText())
        );
    }

    @PostMapping("/todos/{id}/complete")
    public CompleteTodoResponse complete(@PathVariable("id") UUID id) {
        boolean completed = todoApplicationService.completeTodo(id, Instant.now());
        return new CompleteTodoResponse(completed);
    }

    @GetMapping("/todos")
    public ListTodosResponse list(
            @RequestParam(name = "status", defaultValue = "ALL") String status,
            @RequestParam(name = "householdId", required = false) UUID householdId
    ) {
        TodoStatus filter = TodoStatus.valueOf(status);
        return new ListTodosResponse(toResponseItems(
                todoApplicationService.listTodos(householdId, filter)
        ));
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
