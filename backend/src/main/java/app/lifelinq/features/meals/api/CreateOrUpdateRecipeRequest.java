package app.lifelinq.features.meals.api;

import java.util.List;

public final class CreateOrUpdateRecipeRequest {
    private String name;
    private String sourceName;
    private String sourceUrl;
    private String originKind;
    private String shortNote;
    private String instructions;
    private Boolean savedInRecipes;
    private List<IngredientRequest> ingredients;

    public String getName() {
        return name;
    }

    public List<IngredientRequest> getIngredients() {
        return ingredients;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getOriginKind() {
        return originKind;
    }

    public String getShortNote() {
        return shortNote;
    }

    public String getInstructions() {
        return instructions;
    }

    public Boolean getSavedInRecipes() {
        return savedInRecipes;
    }
}
