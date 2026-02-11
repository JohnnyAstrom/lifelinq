package app.lifelinq.config;

import java.util.UUID;

public final class RequestContext {
    private UUID householdId;
    private UUID userId;

    public UUID getHouseholdId() {
        return householdId;
    }

    public void setHouseholdId(UUID householdId) {
        this.householdId = householdId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
