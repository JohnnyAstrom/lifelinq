package app.lifelinq.features.meals.domain;

public final class PlannedMeal {
    private final int dayOfWeek;
    private final MealType mealType;
    private final RecipeRef recipeRef;

    PlannedMeal(int dayOfWeek, MealType mealType, RecipeRef recipeRef) {
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new IllegalArgumentException("dayOfWeek must be between 1 and 7");
        }
        if (mealType == null) {
            throw new IllegalArgumentException("mealType must not be null");
        }
        if (recipeRef == null) {
            throw new IllegalArgumentException("recipeRef must not be null");
        }
        this.dayOfWeek = dayOfWeek;
        this.mealType = mealType;
        this.recipeRef = recipeRef;
    }

    public static PlannedMeal rehydrate(int dayOfWeek, MealType mealType, RecipeRef recipeRef) {
        return new PlannedMeal(dayOfWeek, mealType, recipeRef);
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public MealType getMealType() {
        return mealType;
    }

    public RecipeRef getRecipeRef() {
        return recipeRef;
    }
}
