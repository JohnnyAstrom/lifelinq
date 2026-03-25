package app.lifelinq.features.meals.contract;

import java.util.List;
import java.util.UUID;

public record WeekShoppingReviewView(
        UUID weekPlanId,
        int year,
        int isoWeek,
        UUID assessedShoppingListId,
        String assessedShoppingListName,
        WeekShoppingReviewLinkView reviewLink,
        List<AggregatedIngredientComparisonView> ingredients
) {
}
