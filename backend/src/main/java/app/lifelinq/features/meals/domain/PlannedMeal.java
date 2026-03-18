package app.lifelinq.features.meals.domain;

public final class PlannedMeal {
    private final int dayOfWeek;
    private final MealType mealType;
    private final java.util.UUID recipeId;
    private final String recipeTitleSnapshot;

    PlannedMeal(int dayOfWeek, MealType mealType, java.util.UUID recipeId, String recipeTitleSnapshot) {
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new IllegalArgumentException("dayOfWeek must be between 1 and 7");
        }
        if (mealType == null) {
            throw new IllegalArgumentException("mealType must not be null");
        }
        if (recipeId == null) {
            throw new IllegalArgumentException("recipeId must not be null");
        }
        if (recipeTitleSnapshot == null || recipeTitleSnapshot.isBlank()) {
            throw new IllegalArgumentException("recipeTitleSnapshot must not be blank");
        }
        this.dayOfWeek = dayOfWeek;
        this.mealType = mealType;
        this.recipeId = recipeId;
        this.recipeTitleSnapshot = recipeTitleSnapshot.trim();
    }

    public static PlannedMeal rehydrate(
            int dayOfWeek,
            MealType mealType,
            java.util.UUID recipeId,
            String recipeTitleSnapshot
    ) {
        return new PlannedMeal(dayOfWeek, mealType, recipeId, recipeTitleSnapshot);
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public MealType getMealType() {
        return mealType;
    }

    public java.util.UUID getRecipeId() {
        return recipeId;
    }

    public String getRecipeTitleSnapshot() {
        return recipeTitleSnapshot;
    }
}
