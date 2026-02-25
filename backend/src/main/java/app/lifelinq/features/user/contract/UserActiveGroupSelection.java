package app.lifelinq.features.user.contract;

import java.util.UUID;

public interface UserActiveGroupSelection {
    void setActiveGroup(UUID userId, UUID groupId);
}
