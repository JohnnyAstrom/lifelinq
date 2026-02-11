package app.lifelinq.features.household.domain;

import java.util.UUID;

public final class MembershipId {
    private final UUID householdId;
    private final UUID userId;

    public MembershipId(UUID householdId, UUID userId) {
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
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
