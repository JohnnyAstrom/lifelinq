package app.lifelinq.features.household.application;

import app.lifelinq.features.household.domain.HouseholdRole;
import app.lifelinq.features.household.domain.Membership;

public final class AddMemberToHouseholdUseCase {

    public AddMemberToHouseholdResult execute(AddMemberToHouseholdCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.getHouseholdId() == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        if (command.getUserId() == null) {
            throw new IllegalArgumentException("userId must not be null");
        }

        Membership membership = new Membership(
                command.getHouseholdId(),
                command.getUserId(),
                HouseholdRole.MEMBER
        );

        return new AddMemberToHouseholdResult(
                membership.getHouseholdId(),
                membership.getUserId(),
                membership.getRole()
        );
    }
}
