package app.lifelinq.features.household.api;

import java.util.UUID;

public final class CreateHouseholdRequest {
    private String name;
    private UUID ownerUserId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(UUID ownerUserId) {
        this.ownerUserId = ownerUserId;
    }
}
