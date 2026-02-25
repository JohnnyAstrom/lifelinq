package app.lifelinq.features.group.api;

import java.util.UUID;

public final class AcceptInvitationResponse {
    private final UUID groupId;
    private final UUID userId;

    public AcceptInvitationResponse(UUID groupId, UUID userId) {
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
