package app.lifelinq.features.household.application;

import java.util.UUID;

public final class AcceptInvitationResult {
    private final UUID householdId;
    private final UUID userId;

    public AcceptInvitationResult(UUID householdId, UUID userId) {
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
