package app.lifelinq.features.household.infrastructure;

import app.lifelinq.features.household.domain.HouseholdRole;
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
    private HouseholdRole role;

    protected MembershipEntity() {
    }

    public MembershipEntity(MembershipEntityId id, HouseholdRole role) {
        this.id = id;
        this.role = role;
    }

    public MembershipEntityId getId() {
        return id;
    }

    public HouseholdRole getRole() {
        return role;
    }
}
