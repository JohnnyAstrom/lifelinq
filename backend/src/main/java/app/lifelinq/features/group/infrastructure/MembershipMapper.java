package app.lifelinq.features.group.infrastructure;

import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.group.domain.MembershipId;

public final class MembershipMapper {

    public MembershipEntity toEntity(Membership membership) {
        if (membership == null) {
            throw new IllegalArgumentException("membership must not be null");
        }
        MembershipEntityId id = new MembershipEntityId(
                membership.getGroupId(),
                membership.getUserId()
        );
        return new MembershipEntity(id, membership.getRole());
    }

    public Membership toDomain(MembershipEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity must not be null");
        }
        MembershipEntityId id = entity.getId();
        MembershipId membershipId = new MembershipId(id.getGroupId(), id.getUserId());
        return new Membership(membershipId.getGroupId(), membershipId.getUserId(), entity.getRole());
    }
}
