package app.lifelinq.features.group.application;

import java.util.UUID;

public final class CreateGroupCommand {
    private final String groupName;
    private final UUID ownerUserId;

    public CreateGroupCommand(String groupName, UUID ownerUserId) {
        this.groupName = groupName;
        this.ownerUserId = ownerUserId;
    }

    public String getGroupName() {
        return groupName;
    }

    public UUID getOwnerUserId() {
        return ownerUserId;
    }
}
