package app.lifelinq.config;

import java.util.UUID;

public final class JwtClaims {
    private final UUID groupId;
    private final UUID userId;

    public JwtClaims(UUID groupId, UUID userId) {
        this.groupId = groupId;
        this.userId = userId;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public UUID getUserId() {
        return userId;
    }
}
