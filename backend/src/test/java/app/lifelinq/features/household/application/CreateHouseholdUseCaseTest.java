package app.lifelinq.features.household.application;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class CreateHouseholdUseCaseTest {

    @Test
    void createsHouseholdAndReturnsId() {
        CreateHouseholdUseCase useCase = new CreateHouseholdUseCase();
        CreateHouseholdCommand command = new CreateHouseholdCommand("Home", UUID.randomUUID());

        CreateHouseholdResult result = useCase.execute(command);

        assertNotNull(result);
        assertNotNull(result.getHouseholdId());
    }

    @Test
    void requiresCommand() {
        CreateHouseholdUseCase useCase = new CreateHouseholdUseCase();
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    @Test
    void requiresOwnerUserId() {
        CreateHouseholdUseCase useCase = new CreateHouseholdUseCase();
        CreateHouseholdCommand command = new CreateHouseholdCommand("Home", null);
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }
}
