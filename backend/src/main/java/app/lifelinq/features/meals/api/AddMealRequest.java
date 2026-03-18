package app.lifelinq.features.meals.api;

import java.util.UUID;
import java.util.List;

public final class AddMealRequest {
    private String mealTitle;
    private UUID recipeId;
    private String mealType;
    private UUID targetShoppingListId;
    private List<Integer> selectedIngredientPositions;

    public String getMealTitle() {
        return mealTitle;
    }

    public UUID getRecipeId() {
        return recipeId;
    }

    public String getMealType() {
        return mealType;
    }

    public UUID getTargetShoppingListId() {
        return targetShoppingListId;
    }

    public List<Integer> getSelectedIngredientPositions() {
        return selectedIngredientPositions;
    }
}
