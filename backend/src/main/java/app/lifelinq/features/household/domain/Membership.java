package app.lifelinq.features.household.domain;

import java.util.UUID;

public final class Membership {
    private final UUID householdId;
    private final UUID userId;
    private final HouseholdRole role;

    public Membership(UUID householdId, UUID userId, HouseholdRole role) {
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        if (role == null) {
            throw new IllegalArgumentException("role must not be null");
        }
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
