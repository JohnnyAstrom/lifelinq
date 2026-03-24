package app.lifelinq.features.meals.contract;

import java.util.List;

public record ShoppingDeltaView(
        int unresolvedIngredientCount,
        int partialIngredientCount,
        int missingIngredientCount,
        int unknownIngredientCount,
        List<IngredientCoverageView> unresolvedIngredients
) {
}
