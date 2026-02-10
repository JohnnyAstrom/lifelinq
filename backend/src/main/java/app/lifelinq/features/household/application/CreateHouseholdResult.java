package app.lifelinq.features.household.application;

import java.util.UUID;

public final class CreateHouseholdResult {
    private final UUID householdId;

    public CreateHouseholdResult(UUID householdId) {
        this.householdId = householdId;
    }

    public UUID getHouseholdId() {
        return householdId;
    }
}
