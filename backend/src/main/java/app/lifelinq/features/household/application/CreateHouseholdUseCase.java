package app.lifelinq.features.household.application;

import app.lifelinq.features.household.domain.Household;
import app.lifelinq.features.household.domain.HouseholdRole;
import app.lifelinq.features.household.domain.Membership;
import java.util.UUID;

public final class CreateHouseholdUseCase {

    public CreateHouseholdResult execute(CreateHouseholdCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.getOwnerUserId() == null) {
            throw new IllegalArgumentException("ownerUserId must not be null");
        }

        UUID householdId = UUID.randomUUID();
        Household household = new Household(householdId, command.getHouseholdName());
        Membership ownerMembership = new Membership(household.getId(), command.getOwnerUserId(), HouseholdRole.OWNER);

        return new CreateHouseholdResult(household.getId());
    }
}
