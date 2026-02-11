package app.lifelinq.features.household.infrastructure;

import app.lifelinq.features.household.domain.Membership;
import app.lifelinq.features.household.domain.MembershipId;

public final class MembershipMapper {

    public MembershipEntity toEntity(Membership membership) {
        if (membership == null) {
            throw new IllegalArgumentException("membership must not be null");
        }
        MembershipEntityId id = new MembershipEntityId(
                membership.getHouseholdId(),
                membership.getUserId()
        );
        return new MembershipEntity(id, membership.getRole());
    }

    public Membership toDomain(MembershipEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity must not be null");
        }
        MembershipEntityId id = entity.getId();
        MembershipId membershipId = new MembershipId(id.getHouseholdId(), id.getUserId());
        return new Membership(membershipId.getHouseholdId(), membershipId.getUserId(), entity.getRole());
    }
}
