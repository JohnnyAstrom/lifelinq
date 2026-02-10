package app.lifelinq.features.household.application;

import app.lifelinq.features.household.domain.HouseholdRole;
import java.util.UUID;

public final class AddMemberToHouseholdResult {
    private final UUID householdId;
    private final UUID userId;
    private final HouseholdRole role;

    public AddMemberToHouseholdResult(UUID householdId, UUID userId, HouseholdRole role) {
        this.householdId = householdId;
        this.userId = userId;
        this.role = role;
    }

    public UUID getHouseholdId() {
        return householdId;
    }

    public UUID getUserId() {
        return userId;
    }

    public HouseholdRole getRole() {
        return role;
    }
}
