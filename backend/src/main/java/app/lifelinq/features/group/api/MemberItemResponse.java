package app.lifelinq.features.group.api;

import app.lifelinq.features.group.domain.GroupRole;
import java.util.UUID;

public final class MemberItemResponse {
    private final UUID userId;
    private final GroupRole role;

    public MemberItemResponse(UUID userId, GroupRole role) {
        this.userId = userId;
        this.role = role;
    }

    public UUID getUserId() {
        return userId;
    }

    public GroupRole getRole() {
        return role;
    }
}
