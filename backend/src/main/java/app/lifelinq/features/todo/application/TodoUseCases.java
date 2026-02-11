package app.lifelinq.features.todo.application;

import app.lifelinq.features.todo.domain.TodoRepository;

public final class TodoUseCases {
    private final CreateTodoUseCase createTodoUseCase;
    private final CompleteTodoUseCase completeTodoUseCase;
    private final ListTodosUseCase listTodosUseCase;

    public TodoUseCases(TodoRepository todoRepository) {
        if (todoRepository == null) {
            throw new IllegalArgumentException("todoRepository must not be null");
        }
        this.createTodoUseCase = new CreateTodoUseCase();
        this.completeTodoUseCase = new CompleteTodoUseCase(todoRepository);
        this.listTodosUseCase = new ListTodosUseCase(todoRepository);
    }

    public CreateTodoUseCase createTodo() {
        return createTodoUseCase;
    }

    public CompleteTodoUseCase completeTodo() {
        return completeTodoUseCase;
    }

    public ListTodosUseCase listTodos() {
        return listTodosUseCase;
    }
}
