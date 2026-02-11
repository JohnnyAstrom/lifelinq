package app.lifelinq.features.household.domain;

import java.util.UUID;

public final class Membership {
    private final MembershipId id;
    private final HouseholdRole role;

    public Membership(UUID householdId, UUID userId, HouseholdRole role) {
        if (role == null) {
            throw new IllegalArgumentException("role must not be null");
        }
        this.id = new MembershipId(householdId, userId);
        this.role = role;
    }

    public UUID getHouseholdId() {
        return id.getHouseholdId();
    }

    public UUID getUserId() {
        return id.getUserId();
    }

    public HouseholdRole getRole() {
        return role;
    }

    public MembershipId getId() {
        return id;
    }
}
