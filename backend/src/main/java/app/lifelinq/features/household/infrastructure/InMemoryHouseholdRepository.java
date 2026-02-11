package app.lifelinq.features.household.infrastructure;

import app.lifelinq.features.household.domain.Household;
import app.lifelinq.features.household.domain.HouseholdRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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

    @Override
    public Optional<Household> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return Optional.ofNullable(households.get(id));
    }
}
