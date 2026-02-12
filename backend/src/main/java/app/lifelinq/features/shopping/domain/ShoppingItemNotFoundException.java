package app.lifelinq.features.shopping.domain;

import java.util.UUID;

public final class ShoppingItemNotFoundException extends RuntimeException {
    public ShoppingItemNotFoundException(UUID itemId) {
        super("item not found: " + itemId);
    }
}
