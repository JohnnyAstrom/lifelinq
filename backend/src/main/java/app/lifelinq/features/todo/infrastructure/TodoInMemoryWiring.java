package app.lifelinq.features.todo.infrastructure;

import app.lifelinq.features.todo.application.TodoApplicationService;
import app.lifelinq.features.user.contract.UserProvisioning;

public final class TodoInMemoryWiring {
    private TodoInMemoryWiring() {
    }

    public static TodoApplicationService createUseCases(UserProvisioning userProvisioning) {
        InMemoryTodoRepository repository = new InMemoryTodoRepository();
        return new TodoApplicationService(repository, userProvisioning);
    }
}
