package app.lifelinq.features.household.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.lifelinq.features.household.domain.HouseholdRole;
import app.lifelinq.features.household.domain.Membership;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ListHouseholdMembersUseCaseTest {

    @Test
    void returnsOnlyMembersForHousehold() {
        ListHouseholdMembersUseCase useCase = new ListHouseholdMembersUseCase();
        UUID householdId = UUID.randomUUID();
        UUID otherHouseholdId = UUID.randomUUID();

        Membership member1 = new Membership(householdId, UUID.randomUUID(), HouseholdRole.MEMBER);
        Membership member2 = new Membership(householdId, UUID.randomUUID(), HouseholdRole.OWNER);
        Membership other = new Membership(otherHouseholdId, UUID.randomUUID(), HouseholdRole.MEMBER);

        ListHouseholdMembersCommand command = new ListHouseholdMembersCommand(
                householdId,
                List.of(member1, member2, other)
        );

        ListHouseholdMembersResult result = useCase.execute(command);

        assertEquals(2, result.getMembers().size());
        assertEquals(householdId, result.getMembers().get(0).getHouseholdId());
        assertEquals(householdId, result.getMembers().get(1).getHouseholdId());
    }

    @Test
    void requiresCommand() {
        ListHouseholdMembersUseCase useCase = new ListHouseholdMembersUseCase();
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    @Test
    void requiresHouseholdId() {
        ListHouseholdMembersUseCase useCase = new ListHouseholdMembersUseCase();
        ListHouseholdMembersCommand command = new ListHouseholdMembersCommand(null, List.of());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }

    @Test
    void requiresMemberships() {
        ListHouseholdMembersUseCase useCase = new ListHouseholdMembersUseCase();
        ListHouseholdMembersCommand command = new ListHouseholdMembersCommand(UUID.randomUUID(), null);
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }
}
