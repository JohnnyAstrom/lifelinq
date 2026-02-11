package app.lifelinq.features.household.api;

import java.util.UUID;

public final class AcceptInvitationResponse {
    private final UUID householdId;
    private final UUID userId;

    public AcceptInvitationResponse(UUID householdId, UUID userId) {
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
