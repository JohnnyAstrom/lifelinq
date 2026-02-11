package app.lifelinq.features.todo.api;

import java.util.List;

public final class ListTodosResponse {
    private final List<TodoItemResponse> todos;

    public ListTodosResponse(List<TodoItemResponse> todos) {
        this.todos = todos;
    }

    public List<TodoItemResponse> getTodos() {
        return todos;
    }
}
