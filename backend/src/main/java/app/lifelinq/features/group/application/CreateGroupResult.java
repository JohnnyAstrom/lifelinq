package app.lifelinq.features.group.application;

import java.util.UUID;

public final class CreateGroupResult {
    private final UUID groupId;

    public CreateGroupResult(UUID groupId) {
        this.groupId = groupId;
    }

    public UUID getGroupId() {
        return groupId;
    }
}
