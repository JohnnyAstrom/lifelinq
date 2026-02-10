package app.lifelinq.features.household.domain;

import java.util.List;
import java.util.UUID;

public interface MembershipRepository {
    void save(Membership membership);

    List<Membership> findByHouseholdId(UUID householdId);

    boolean deleteByHouseholdIdAndUserId(UUID householdId, UUID userId);
}
