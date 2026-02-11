package app.lifelinq.features.household.infrastructure;

import app.lifelinq.features.household.domain.Household;

public final class HouseholdMapper {

    public HouseholdEntity toEntity(Household household) {
        if (household == null) {
            throw new IllegalArgumentException("household must not be null");
        }
        return new HouseholdEntity(household.getId(), household.getName());
    }

    public Household toDomain(HouseholdEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity must not be null");
        }
        return new Household(entity.getId(), entity.getName());
    }
}
