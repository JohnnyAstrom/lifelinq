package app.lifelinq.features.household.application;

import app.lifelinq.features.household.domain.Membership;
import java.util.List;
import java.util.UUID;

public final class ListHouseholdMembersCommand {
    private final UUID householdId;
    private final List<Membership> memberships;

    public ListHouseholdMembersCommand(UUID householdId, List<Membership> memberships) {
        this.householdId = householdId;
        this.memberships = memberships;
    }

    public UUID getHouseholdId() {
        return householdId;
    }

    public List<Membership> getMemberships() {
        return memberships;
    }
}
