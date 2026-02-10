package app.lifelinq.features.household.infrastructure;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import app.lifelinq.features.household.application.HouseholdUseCases;
import org.junit.jupiter.api.Test;

class HouseholdInMemoryWiringTest {

    @Test
    void createsAllUseCases() {
        HouseholdUseCases useCases = HouseholdInMemoryWiring.createUseCases();

        assertNotNull(useCases);
        assertNotNull(useCases.createHousehold());
        assertNotNull(useCases.addMember());
        assertNotNull(useCases.listMembers());
        assertNotNull(useCases.removeMember());
        assertNotNull(useCases.createInvitation());
        assertNotNull(useCases.acceptInvitation());
    }
}
