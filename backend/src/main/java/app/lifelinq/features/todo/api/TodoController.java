package app.lifelinq.features.todo.api;

import app.lifelinq.config.RequestContext;
import app.lifelinq.features.api.ApiScoping;
import app.lifelinq.features.todo.application.TodoApplicationService;
import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> create(@RequestBody CreateTodoRequest request) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getHouseholdId() == null) {
            return ApiScoping.missingContext();
        }
        return ResponseEntity.ok(new CreateTodoResponse(
                todoApplicationService.createTodo(context.getHouseholdId(), request.getText())
        ));
    }

    @PostMapping("/todos/{id}/complete")
    public CompleteTodoResponse complete(@PathVariable("id") UUID id) {
        boolean completed = todoApplicationService.completeTodo(id, Instant.now());
        return new CompleteTodoResponse(completed);
    }

    @GetMapping("/todos")
    public ResponseEntity<?> list(
            @RequestParam(name = "status", defaultValue = "ALL") String status
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getHouseholdId() == null) {
            return ApiScoping.missingContext();
        }
        TodoStatus filter = TodoStatus.valueOf(status);
        return ResponseEntity.ok(new ListTodosResponse(toResponseItems(
                todoApplicationService.listTodos(context.getHouseholdId(), filter)
        )));
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
