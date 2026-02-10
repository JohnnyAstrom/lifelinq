package app.lifelinq.features.household.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.lifelinq.features.household.domain.HouseholdRole;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AddMemberToHouseholdUseCaseTest {

    @Test
    void addsMemberWithMemberRole() {
        AddMemberToHouseholdUseCase useCase = new AddMemberToHouseholdUseCase();
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AddMemberToHouseholdCommand command = new AddMemberToHouseholdCommand(householdId, userId);

        AddMemberToHouseholdResult result = useCase.execute(command);

        assertEquals(householdId, result.getHouseholdId());
        assertEquals(userId, result.getUserId());
        assertEquals(HouseholdRole.MEMBER, result.getRole());
    }

    @Test
    void requiresCommand() {
        AddMemberToHouseholdUseCase useCase = new AddMemberToHouseholdUseCase();
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    @Test
    void requiresHouseholdId() {
        AddMemberToHouseholdUseCase useCase = new AddMemberToHouseholdUseCase();
        AddMemberToHouseholdCommand command = new AddMemberToHouseholdCommand(null, UUID.randomUUID());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }

    @Test
    void requiresUserId() {
        AddMemberToHouseholdUseCase useCase = new AddMemberToHouseholdUseCase();
        AddMemberToHouseholdCommand command = new AddMemberToHouseholdCommand(UUID.randomUUID(), null);
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }
}
