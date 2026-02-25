package app.lifelinq.features.group.api;

import java.util.UUID;

public final class CreateGroupResponse {
    private final UUID groupId;

    public CreateGroupResponse(UUID groupId) {
        this.groupId = groupId;
    }

    public UUID getGroupId() {
        return groupId;
    }
}
