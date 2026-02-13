package app.lifelinq.features.todo.api;

import app.lifelinq.config.RequestContext;
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
        if (context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        return ResponseEntity.ok(new CreateTodoResponse(
                todoApplicationService.createTodo(
                        context.getHouseholdId(),
                        context.getUserId(),
                        request.getText(),
                        request.getDueDate(),
                        request.getDueTime()
                )
        ));
    }

    @PostMapping("/todos/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable("id") UUID id) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        boolean completed = todoApplicationService.completeTodo(id, context.getUserId(), Instant.now());
        return ResponseEntity.ok(new CompleteTodoResponse(completed));
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
                    todo.getStatus(),
                    todo.getDueDate(),
                    todo.getDueTime()
            ));
        }
        return items;
    }
}
