package app.lifelinq.features.household.application;

import app.lifelinq.features.household.domain.Membership;
import java.util.List;

public final class ListHouseholdMembersResult {
    private final List<Membership> members;

    public ListHouseholdMembersResult(List<Membership> members) {
        this.members = members;
    }

    public List<Membership> getMembers() {
        return members;
    }
}
