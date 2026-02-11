package app.lifelinq.features.household.api;

import java.util.UUID;

public final class CreateHouseholdResponse {
    private final UUID householdId;

    public CreateHouseholdResponse(UUID householdId) {
        this.householdId = householdId;
    }

    public UUID getHouseholdId() {
        return householdId;
    }
}
