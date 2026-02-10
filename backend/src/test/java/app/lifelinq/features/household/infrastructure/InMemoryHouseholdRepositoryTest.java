package app.lifelinq.features.household.infrastructure;

import static org.junit.jupiter.api.Assertions.assertThrows;

import app.lifelinq.features.household.domain.Household;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InMemoryHouseholdRepositoryTest {

    @Test
    void requiresHousehold() {
        InMemoryHouseholdRepository repository = new InMemoryHouseholdRepository();
        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
    }

    @Test
    void savesHousehold() {
        InMemoryHouseholdRepository repository = new InMemoryHouseholdRepository();
        Household household = new Household(UUID.randomUUID(), "Home");
        repository.save(household);
    }
}
