package app.lifelinq.features.meals.contract;

import java.time.Instant;
import java.util.UUID;

public record WeekShoppingReviewLinkView(
        UUID shoppingListId,
        String shoppingListName,
        Instant reviewedAt
) {
}
