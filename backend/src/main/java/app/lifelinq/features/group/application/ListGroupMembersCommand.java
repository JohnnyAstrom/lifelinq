package app.lifelinq.features.group.application;

import java.util.UUID;

public final class ListGroupMembersCommand {
    private final UUID groupId;

    public ListGroupMembersCommand(UUID groupId) {
        this.groupId = groupId;
    }

    public UUID getGroupId() {
        return groupId;
    }
}
