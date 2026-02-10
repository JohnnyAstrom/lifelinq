package app.lifelinq.features.todo.application;

import app.lifelinq.features.todo.domain.Todo;
import java.util.List;

public final class ListTodosResult {
    private final List<Todo> todos;

    public ListTodosResult(List<Todo> todos) {
        this.todos = todos;
    }

    public List<Todo> getTodos() {
        return todos;
    }
}
