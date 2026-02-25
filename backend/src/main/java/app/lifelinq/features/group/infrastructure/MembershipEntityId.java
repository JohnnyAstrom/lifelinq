package app.lifelinq.features.group.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import java.util.Objects;

@Embeddable
public class MembershipEntityId implements Serializable {
    @Column(nullable = false)
    private UUID groupId;

    @Column(nullable = false)
    private UUID userId;

    protected MembershipEntityId() {
    }

    public MembershipEntityId(UUID groupId, UUID userId) {
        this.groupId = groupId;
        this.userId = userId;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MembershipEntityId that = (MembershipEntityId) o;
        return Objects.equals(groupId, that.groupId)
                && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, userId);
    }
}
