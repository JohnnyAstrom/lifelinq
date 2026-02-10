package app.lifelinq.features.household.application;

import app.lifelinq.features.household.domain.Membership;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ListHouseholdMembersUseCase {

    public ListHouseholdMembersResult execute(ListHouseholdMembersCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.getHouseholdId() == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        if (command.getMemberships() == null) {
            throw new IllegalArgumentException("memberships must not be null");
        }

        UUID householdId = command.getHouseholdId();
        List<Membership> filtered = new ArrayList<>();
        for (Membership membership : command.getMemberships()) {
            if (membership != null && householdId.equals(membership.getHouseholdId())) {
                filtered.add(membership);
            }
        }

        return new ListHouseholdMembersResult(filtered);
    }
}
