package app.lifelinq.features.group.api;

import app.lifelinq.features.group.domain.GroupRole;
import java.util.UUID;

public final class AddMemberResponse {
    private final UUID groupId;
    private final UUID userId;
    private final GroupRole role;

    public AddMemberResponse(UUID groupId, UUID userId, GroupRole role) {
        this.groupId = groupId;
        this.userId = userId;
        this.role = role;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public UUID getUserId() {
        return userId;
    }

    public GroupRole getRole() {
        return role;
    }
}
