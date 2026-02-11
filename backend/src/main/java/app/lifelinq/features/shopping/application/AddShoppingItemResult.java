package app.lifelinq.features.shopping.application;

import java.util.UUID;

public final class AddShoppingItemResult {
    private final UUID itemId;

    public AddShoppingItemResult(UUID itemId) {
        this.itemId = itemId;
    }

    public UUID getItemId() {
        return itemId;
    }
}
