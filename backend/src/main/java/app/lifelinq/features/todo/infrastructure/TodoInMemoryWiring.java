package app.lifelinq.features.todo.infrastructure;

import app.lifelinq.features.todo.application.TodoUseCases;

public final class TodoInMemoryWiring {
    private TodoInMemoryWiring() {
    }

    public static TodoUseCases createUseCases() {
        InMemoryTodoRepository repository = new InMemoryTodoRepository();
        return new TodoUseCases(repository);
    }
}
