package app.lifelinq.features.todo.application;

import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoRepository;
import app.lifelinq.features.todo.domain.TodoStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ListTodosUseCase {
    private final TodoRepository todoRepository;

    public ListTodosUseCase(TodoRepository todoRepository) {
        if (todoRepository == null) {
            throw new IllegalArgumentException("todoRepository must not be null");
        }
        this.todoRepository = todoRepository;
    }

    public ListTodosResult execute(TodoQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("query must not be null");
        }
        if (query.getStatusFilter() == null) {
            throw new IllegalArgumentException("statusFilter must not be null");
        }

        List<Todo> all = todoRepository.findAll();
        return new ListTodosResult(filter(all, query.getHouseholdId(), query.getStatusFilter()));
    }

    private List<Todo> filter(List<Todo> todos, UUID householdId, TodoStatus statusFilter) {
        List<Todo> result = new ArrayList<>();
        for (Todo todo : todos) {
            if (todo == null) {
                continue;
            }
            if (householdId != null && !householdId.equals(todo.getHouseholdId())) {
                continue;
            }
            if (statusFilter != TodoStatus.ALL && todo.getStatus() != statusFilter) {
                continue;
            }
            result.add(todo);
        }
        return result;
    }
}
