package app.lifelinq.features.household.api;

import app.lifelinq.features.household.domain.HouseholdRole;
import java.util.UUID;

public final class MemberItemResponse {
    private final UUID userId;
    private final HouseholdRole role;

    public MemberItemResponse(UUID userId, HouseholdRole role) {
        this.userId = userId;
        this.role = role;
    }

    public UUID getUserId() {
        return userId;
    }

    public HouseholdRole getRole() {
        return role;
    }
}
