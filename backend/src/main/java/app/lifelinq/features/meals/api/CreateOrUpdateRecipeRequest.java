package app.lifelinq.features.meals.api;

import java.util.List;

public final class CreateOrUpdateRecipeRequest {
    private String name;
    private String source;
    private String shortNote;
    private String instructions;
    private List<IngredientRequest> ingredients;

    public String getName() {
        return name;
    }

    public List<IngredientRequest> getIngredients() {
        return ingredients;
    }

    public String getSource() {
        return source;
    }

    public String getShortNote() {
        return shortNote;
    }

    public String getInstructions() {
        return instructions;
    }
}
