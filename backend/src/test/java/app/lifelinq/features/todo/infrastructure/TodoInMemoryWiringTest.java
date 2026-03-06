package app.lifelinq.features.todo.infrastructure;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import app.lifelinq.features.group.contract.EnsureGroupMemberUseCase;
import app.lifelinq.features.todo.application.TodoApplicationService;
import org.junit.jupiter.api.Test;

class TodoInMemoryWiringTest {

    @Test
    void createsApplicationService() {
        EnsureGroupMemberUseCase ensureGroupMemberUseCase = (groupId, actorUserId) -> { };
        TodoApplicationService service = TodoInMemoryWiring.createUseCases(
                ensureGroupMemberUseCase
        );

        assertNotNull(service);
    }
}
