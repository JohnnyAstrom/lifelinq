package app.lifelinq.features.meals.api;

import java.util.List;
import java.util.UUID;

public final class AddWeekShoppingReviewLinesRequest {
    private UUID shoppingListId;
    private List<String> selectedLineIds;

    public UUID getShoppingListId() {
        return shoppingListId;
    }

    public List<String> getSelectedLineIds() {
        return selectedLineIds;
    }
}
