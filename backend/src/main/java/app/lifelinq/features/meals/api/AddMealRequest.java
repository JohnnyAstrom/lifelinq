package app.lifelinq.features.meals.api;

import java.util.UUID;

public final class AddMealRequest {
    private UUID recipeId;
    private String recipeTitle;
    private String mealType;
    private UUID targetShoppingListId;

    public UUID getRecipeId() {
        return recipeId;
    }

    public String getRecipeTitle() {
        return recipeTitle;
    }

    public String getMealType() {
        return mealType;
    }

    public UUID getTargetShoppingListId() {
        return targetShoppingListId;
    }
}
