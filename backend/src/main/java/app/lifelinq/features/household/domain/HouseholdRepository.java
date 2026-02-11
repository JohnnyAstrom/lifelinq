package app.lifelinq.features.household.domain;

import java.util.Optional;
import java.util.UUID;

public interface HouseholdRepository {
    void save(Household household);

    Optional<Household> findById(UUID id);
}
