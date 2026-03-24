package app.lifelinq.features.meals.api;

import java.util.List;

public final class UpdateRecipeDraftRequest {
    private String name;
    private String sourceName;
    private String sourceUrl;
    private String servings;
    private String shortNote;
    private String instructions;
    private Boolean markReady;
    private List<IngredientRequest> ingredients;

    public String getName() {
        return name;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getServings() {
        return servings;
    }

    public String getShortNote() {
        return shortNote;
    }

    public String getInstructions() {
        return instructions;
    }

    public Boolean getMarkReady() {
        return markReady;
    }

    public List<IngredientRequest> getIngredients() {
        return ingredients;
    }
}
