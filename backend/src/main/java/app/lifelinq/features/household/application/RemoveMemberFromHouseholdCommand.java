package app.lifelinq.features.household.application;

import java.util.UUID;

public final class RemoveMemberFromHouseholdCommand {
    private final UUID householdId;
    private final UUID userId;

    public RemoveMemberFromHouseholdCommand(UUID householdId, UUID userId) {
        this.householdId = householdId;
        this.userId = userId;
    }

    public UUID getHouseholdId() {
        return householdId;
    }

    public UUID getUserId() {
        return userId;
    }
}
