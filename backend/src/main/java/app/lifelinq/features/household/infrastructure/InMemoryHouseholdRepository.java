package app.lifelinq.features.household.infrastructure;

import app.lifelinq.features.household.domain.Household;
import app.lifelinq.features.household.domain.HouseholdRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class InMemoryHouseholdRepository implements HouseholdRepository {
    private final Map<UUID, Household> households = new HashMap<>();

    @Override
    public void save(Household household) {
        if (household == null) {
            throw new IllegalArgumentException("household must not be null");
        }
        households.put(household.getId(), household);
    }
}
