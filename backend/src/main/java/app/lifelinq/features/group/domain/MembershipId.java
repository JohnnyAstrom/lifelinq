package app.lifelinq.features.group.domain;

import java.util.UUID;

public final class MembershipId {
    private final UUID groupId;
    private final UUID userId;

    public MembershipId(UUID groupId, UUID userId) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
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
