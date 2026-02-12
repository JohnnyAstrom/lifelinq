package app.lifelinq.features.shopping.domain;

import java.util.UUID;

public final class ShoppingListNotFoundException extends RuntimeException {
    public ShoppingListNotFoundException(UUID listId) {
        super("list not found: " + listId);
    }
}
