package app.lifelinq.features.meals.domain;

import java.time.Instant;
import java.util.UUID;

public record WeekShoppingReviewLink(
        UUID shoppingListId,
        Instant reviewedAt
) {
    public WeekShoppingReviewLink {
        if (shoppingListId == null) {
            throw new IllegalArgumentException("shoppingListId must not be null");
        }
        if (reviewedAt == null) {
            throw new IllegalArgumentException("reviewedAt must not be null");
        }
    }
}
