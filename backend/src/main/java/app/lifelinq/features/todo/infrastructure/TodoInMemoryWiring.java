package app.lifelinq.features.todo.infrastructure;

import app.lifelinq.features.todo.application.TodoApplicationService;

public final class TodoInMemoryWiring {
    private TodoInMemoryWiring() {
    }

    public static TodoApplicationService createUseCases() {
        InMemoryTodoRepository repository = new InMemoryTodoRepository();
        return new TodoApplicationService(repository);
    }
}
