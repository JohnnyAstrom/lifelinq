package app.lifelinq.features.todo.application;

import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoRepository;
import app.lifelinq.features.todo.domain.TodoStatus;
import java.util.List;

final class ListTodosUseCase {
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
        if (query.getHouseholdId() == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }

        List<Todo> items = todoRepository.listByHousehold(query.getHouseholdId(), query.getStatusFilter());
        return new ListTodosResult(items);
    }
}
