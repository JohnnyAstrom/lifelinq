package app.lifelinq.features.meals.contract;

import java.util.List;
import java.util.UUID;

public record MealShoppingProjectionView(
        int year,
        int isoWeek,
        int dayOfWeek,
        String mealType,
        String mealTitle,
        UUID recipeId,
        String recipeTitle,
        boolean recipeBacked,
        ShoppingLinkReferenceView shoppingLink,
        MealReadinessView readiness,
        ShoppingDeltaView delta,
        List<IngredientCoverageView> ingredientCoverage
) {
}
