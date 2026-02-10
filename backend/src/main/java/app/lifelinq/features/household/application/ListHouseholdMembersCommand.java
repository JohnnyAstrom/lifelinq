package app.lifelinq.features.household.application;

import java.util.UUID;

public final class ListHouseholdMembersCommand {
    private final UUID householdId;

    public ListHouseholdMembersCommand(UUID householdId) {
        this.householdId = householdId;
    }

    public UUID getHouseholdId() {
        return householdId;
    }
}
