package app.lifelinq.features.group.application;

import java.util.UUID;

public final class AcceptInvitationResult {
    private final UUID groupId;
    private final UUID userId;

    public AcceptInvitationResult(UUID groupId, UUID userId) {
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
