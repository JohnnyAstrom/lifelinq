package app.lifelinq.features.shopping.api;

import java.util.UUID;

public final class CreateShoppingItemRequest {
    private UUID householdId;
    private String name;

    public UUID getHouseholdId() {
        return householdId;
    }

    public void setHouseholdId(UUID householdId) {
        this.householdId = householdId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
