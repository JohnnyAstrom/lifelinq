package app.lifelinq.features.meals.domain;

public final class PlannedMeal {
    private final int dayOfWeek;
    private final RecipeRef recipeRef;

    PlannedMeal(int dayOfWeek, RecipeRef recipeRef) {
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new IllegalArgumentException("dayOfWeek must be between 1 and 7");
        }
        if (recipeRef == null) {
            throw new IllegalArgumentException("recipeRef must not be null");
        }
        this.dayOfWeek = dayOfWeek;
        this.recipeRef = recipeRef;
    }

    public static PlannedMeal rehydrate(int dayOfWeek, RecipeRef recipeRef) {
        return new PlannedMeal(dayOfWeek, recipeRef);
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public RecipeRef getRecipeRef() {
        return recipeRef;
    }
}
