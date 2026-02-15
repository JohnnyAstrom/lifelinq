package app.lifelinq.features.todo.infrastructure;

import app.lifelinq.features.todo.application.TodoApplicationService;
import app.lifelinq.features.user.application.UserApplicationService;

public final class TodoInMemoryWiring {
    private TodoInMemoryWiring() {
    }

    public static TodoApplicationService createUseCases(UserApplicationService userApplicationService) {
        InMemoryTodoRepository repository = new InMemoryTodoRepository();
        return new TodoApplicationService(repository, userApplicationService);
    }
}
