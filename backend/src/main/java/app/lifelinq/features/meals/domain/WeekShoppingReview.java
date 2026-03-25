package app.lifelinq.features.meals.domain;

import java.util.List;
import java.util.UUID;

public record WeekShoppingReview(
        UUID weekPlanId,
        int year,
        int isoWeek,
        UUID assessedShoppingListId,
        WeekShoppingReviewLink reviewLink,
        List<AggregatedIngredientComparison> ingredients
) {
    public WeekShoppingReview {
        if (isoWeek < 1 || isoWeek > 53) {
            throw new IllegalArgumentException("isoWeek must be between 1 and 53");
        }
        if (ingredients == null) {
            throw new IllegalArgumentException("ingredients must not be null");
        }
        ingredients = List.copyOf(ingredients);
    }
}
