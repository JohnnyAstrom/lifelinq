package app.lifelinq.features.group.domain;

import java.util.UUID;

public final class Membership {
    private final MembershipId id;
    private final GroupRole role;

    public Membership(UUID groupId, UUID userId, GroupRole role) {
        if (role == null) {
            throw new IllegalArgumentException("role must not be null");
        }
        this.id = new MembershipId(groupId, userId);
        this.role = role;
    }

    public UUID getGroupId() {
        return id.getGroupId();
    }

    public UUID getUserId() {
        return id.getUserId();
    }

    public GroupRole getRole() {
        return role;
    }

    public MembershipId getId() {
        return id;
    }
}
