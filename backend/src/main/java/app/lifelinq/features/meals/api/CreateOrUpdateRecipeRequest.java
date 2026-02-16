package app.lifelinq.features.meals.api;

import java.util.List;

public final class CreateOrUpdateRecipeRequest {
    private String name;
    private List<IngredientRequest> ingredients;

    public String getName() {
        return name;
    }

    public List<IngredientRequest> getIngredients() {
        return ingredients;
    }
}
