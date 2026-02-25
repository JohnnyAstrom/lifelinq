package app.lifelinq.features.group.infrastructure;

import app.lifelinq.features.group.domain.GroupRole;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "memberships")
public class MembershipEntity {
    @EmbeddedId
    private MembershipEntityId id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupRole role;

    protected MembershipEntity() {
    }

    public MembershipEntity(MembershipEntityId id, GroupRole role) {
        this.id = id;
        this.role = role;
    }

    public MembershipEntityId getId() {
        return id;
    }

    public GroupRole getRole() {
        return role;
    }
}
