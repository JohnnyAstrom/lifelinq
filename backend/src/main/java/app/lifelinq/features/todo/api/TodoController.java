package app.lifelinq.features.todo.api;

import app.lifelinq.config.RequestContext;
import app.lifelinq.features.todo.application.TodoApplicationService;
import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoScope;
import app.lifelinq.features.todo.domain.TodoStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
                        resolveScope(request.getScope(), request.getDueDate()),
                        request.getDueDate(),
                        request.getDueTime(),
                        request.getScopeYear(),
                        request.getScopeWeek(),
                        request.getScopeMonth()
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

    @PutMapping("/todos/{id}")
    public ResponseEntity<?> update(@PathVariable("id") UUID id, @RequestBody UpdateTodoRequest request) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        boolean updated = todoApplicationService.updateTodo(
                id,
                context.getUserId(),
                request.getText(),
                resolveScope(request.getScope(), request.getDueDate()),
                request.getDueDate(),
                request.getDueTime(),
                request.getScopeYear(),
                request.getScopeWeek(),
                request.getScopeMonth()
        );
        return ResponseEntity.ok(new UpdateTodoResponse(updated));
    }

    @DeleteMapping("/todos/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") UUID id) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        boolean deleted = todoApplicationService.deleteTodo(id, context.getUserId(), Instant.now());
        return ResponseEntity.ok(new UpdateTodoResponse(deleted));
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

    @GetMapping("/todos/calendar/{year}/{month}")
    public ResponseEntity<?> listForMonth(
            @PathVariable int year,
            @PathVariable int month
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getHouseholdId() == null) {
            return ApiScoping.missingContext();
        }
        if (month < 1 || month > 12) {
            return ResponseEntity.badRequest().body("month must be between 1 and 12");
        }
        return ResponseEntity.ok(new ListTodosResponse(toResponseItems(
                todoApplicationService.listTodosForMonth(context.getHouseholdId(), year, month)
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
                    todo.getScope(),
                    todo.getDueDate(),
                    todo.getDueTime(),
                    todo.getScopeYear(),
                    todo.getScopeWeek(),
                    todo.getScopeMonth(),
                    todo.getCompletedAt(),
                    todo.getCreatedAt()
            ));
        }
        return items;
    }

    private TodoScope resolveScope(TodoScope requestedScope, java.time.LocalDate dueDate) {
        if (requestedScope != null) {
            return requestedScope;
        }
        return dueDate != null ? TodoScope.DAY : TodoScope.LATER;
    }
}
