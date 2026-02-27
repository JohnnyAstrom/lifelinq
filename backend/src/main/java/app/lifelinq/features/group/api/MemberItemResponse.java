package app.lifelinq.features.group.api;

import app.lifelinq.features.group.domain.GroupRole;
import java.util.UUID;

public final class MemberItemResponse {
    private final UUID userId;
    private final GroupRole role;
    private final String displayName;

    public MemberItemResponse(UUID userId, GroupRole role, String displayName) {
        this.userId = userId;
        this.role = role;
        this.displayName = displayName;
    }

    public UUID getUserId() {
        return userId;
    }

    public GroupRole getRole() {
        return role;
    }

    public String getDisplayName() {
        return displayName;
    }
}
