package app.lifelinq.features.household.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import java.util.Objects;

@Embeddable
public class MembershipEntityId implements Serializable {
    @Column(nullable = false)
    private UUID householdId;

    @Column(nullable = false)
    private UUID userId;

    protected MembershipEntityId() {
    }

    public MembershipEntityId(UUID householdId, UUID userId) {
        this.householdId = householdId;
        this.userId = userId;
    }

    public UUID getHouseholdId() {
        return householdId;
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
        return Objects.equals(householdId, that.householdId)
                && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(householdId, userId);
    }
}
