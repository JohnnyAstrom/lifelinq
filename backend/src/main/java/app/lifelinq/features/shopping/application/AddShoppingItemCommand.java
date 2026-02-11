package app.lifelinq.features.shopping.application;

import java.util.UUID;

public final class AddShoppingItemCommand {
    private final UUID householdId;
    private final String name;

    public AddShoppingItemCommand(UUID householdId, String name) {
        this.householdId = householdId;
        this.name = name;
    }

    public UUID getHouseholdId() {
        return householdId;
    }

    public String getName() {
        return name;
    }
}
