package app.lifelinq.features.todo.infrastructure;

import app.lifelinq.features.todo.application.TodoApplicationService;
import app.lifelinq.features.group.contract.EnsureGroupMemberUseCase;

public final class TodoInMemoryWiring {
    private TodoInMemoryWiring() {
    }

    public static TodoApplicationService createUseCases(EnsureGroupMemberUseCase ensureGroupMemberUseCase) {
        InMemoryTodoRepository repository = new InMemoryTodoRepository();
        return new TodoApplicationService(repository, ensureGroupMemberUseCase);
    }
}
