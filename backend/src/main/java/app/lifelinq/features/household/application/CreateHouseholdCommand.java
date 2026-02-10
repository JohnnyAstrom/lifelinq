package app.lifelinq.features.household.application;

import java.util.UUID;

public final class CreateHouseholdCommand {
    private final String householdName;
    private final UUID ownerUserId;

    public CreateHouseholdCommand(String householdName, UUID ownerUserId) {
        this.householdName = householdName;
        this.ownerUserId = ownerUserId;
    }

    public String getHouseholdName() {
        return householdName;
    }

    public UUID getOwnerUserId() {
        return ownerUserId;
    }
}
