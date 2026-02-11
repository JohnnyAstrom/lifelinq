package app.lifelinq.config;

import java.util.UUID;

public final class JwtClaims {
    private final UUID householdId;
    private final UUID userId;

    public JwtClaims(UUID householdId, UUID userId) {
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
