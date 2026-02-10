package app.lifelinq.features.household.infrastructure;

import app.lifelinq.features.household.domain.Membership;
import app.lifelinq.features.household.domain.MembershipRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class InMemoryMembershipRepository implements MembershipRepository {
    private final List<Membership> memberships = new ArrayList<>();

    @Override
    public void save(Membership membership) {
        if (membership == null) {
            throw new IllegalArgumentException("membership must not be null");
        }
        memberships.add(membership);
    }

    @Override
    public List<Membership> findByHouseholdId(UUID householdId) {
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        List<Membership> result = new ArrayList<>();
        for (Membership membership : memberships) {
            if (householdId.equals(membership.getHouseholdId())) {
                result.add(membership);
            }
        }
        return result;
    }

    @Override
    public boolean deleteByHouseholdIdAndUserId(UUID householdId, UUID userId) {
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        for (int i = 0; i < memberships.size(); i++) {
            Membership membership = memberships.get(i);
            if (householdId.equals(membership.getHouseholdId()) && userId.equals(membership.getUserId())) {
                memberships.remove(i);
                return true;
            }
        }
        return false;
    }
}
