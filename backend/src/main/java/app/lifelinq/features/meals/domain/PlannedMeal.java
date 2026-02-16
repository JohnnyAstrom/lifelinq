package app.lifelinq.features.meals.domain;

public final class PlannedMeal {
    private final int dayOfWeek;
    private final MealType mealType;
    private final java.util.UUID recipeId;

    PlannedMeal(int dayOfWeek, MealType mealType, java.util.UUID recipeId) {
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new IllegalArgumentException("dayOfWeek must be between 1 and 7");
        }
        if (mealType == null) {
            throw new IllegalArgumentException("mealType must not be null");
        }
        if (recipeId == null) {
            throw new IllegalArgumentException("recipeId must not be null");
        }
        this.dayOfWeek = dayOfWeek;
        this.mealType = mealType;
        this.recipeId = recipeId;
    }

    public static PlannedMeal rehydrate(int dayOfWeek, MealType mealType, java.util.UUID recipeId) {
        return new PlannedMeal(dayOfWeek, mealType, recipeId);
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
}
