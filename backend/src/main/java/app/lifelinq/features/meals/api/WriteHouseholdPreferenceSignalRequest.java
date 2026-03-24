package app.lifelinq.features.meals.api;

import java.util.UUID;

public class WriteHouseholdPreferenceSignalRequest {
    private String targetKind;
    private String signalType;
    private UUID recipeId;
    private String mealIdentityKey;

    public String getTargetKind() {
        return targetKind;
    }

    public void setTargetKind(String targetKind) {
        this.targetKind = targetKind;
    }

    public String getSignalType() {
        return signalType;
    }

    public void setSignalType(String signalType) {
        this.signalType = signalType;
    }

    public UUID getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(UUID recipeId) {
        this.recipeId = recipeId;
    }

    public String getMealIdentityKey() {
        return mealIdentityKey;
    }

    public void setMealIdentityKey(String mealIdentityKey) {
        this.mealIdentityKey = mealIdentityKey;
    }
}
