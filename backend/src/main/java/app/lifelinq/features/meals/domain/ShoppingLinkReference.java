package app.lifelinq.features.meals.domain;

import java.time.Instant;
import java.util.UUID;

public record ShoppingLinkReference(
        UUID shoppingListId,
        String shoppingListName,
        Instant shoppingHandledAt,
        ShoppingLinkStatus status
) {
    public ShoppingLinkReference {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
    }
}
