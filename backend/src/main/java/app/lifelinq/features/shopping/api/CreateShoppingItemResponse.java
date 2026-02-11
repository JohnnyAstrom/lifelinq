package app.lifelinq.features.shopping.api;

import java.util.UUID;

public final class CreateShoppingItemResponse {
    private final UUID itemId;

    public CreateShoppingItemResponse(UUID itemId) {
        this.itemId = itemId;
    }

    public UUID getItemId() {
        return itemId;
    }
}
