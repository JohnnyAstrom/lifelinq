package app.lifelinq.features.meals.contract;

import java.time.Instant;
import java.util.UUID;

public record ShoppingLinkReferenceView(
        UUID shoppingListId,
        String shoppingListName,
        Instant shoppingHandledAt,
        String status
) {
}
