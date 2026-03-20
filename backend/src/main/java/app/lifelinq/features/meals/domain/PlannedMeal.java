package app.lifelinq.features.meals.domain;

public final class PlannedMeal {
    private final int dayOfWeek;
    private final MealType mealType;
    private final String mealTitle;
    private final java.util.UUID recipeId;
    private final String recipeTitleSnapshot;
    private final java.time.Instant shoppingHandledAt;
    private final java.util.UUID shoppingListId;

    PlannedMeal(
            int dayOfWeek,
            MealType mealType,
            String mealTitle,
            java.util.UUID recipeId,
            String recipeTitleSnapshot,
            java.time.Instant shoppingHandledAt,
            java.util.UUID shoppingListId
    ) {
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new IllegalArgumentException("dayOfWeek must be between 1 and 7");
        }
        if (mealType == null) {
            throw new IllegalArgumentException("mealType must not be null");
        }
        if (mealTitle == null || mealTitle.isBlank()) {
            throw new IllegalArgumentException("mealTitle must not be blank");
        }
        if (recipeId == null && recipeTitleSnapshot != null && !recipeTitleSnapshot.isBlank()) {
            throw new IllegalArgumentException("recipeTitleSnapshot requires recipeId");
        }
        if (recipeId != null && (recipeTitleSnapshot == null || recipeTitleSnapshot.isBlank())) {
            throw new IllegalArgumentException("recipeTitleSnapshot must not be blank when recipeId is set");
        }
        if ((shoppingHandledAt == null) != (shoppingListId == null)) {
            throw new IllegalArgumentException("shoppingHandledAt and shoppingListId must be set together");
        }
        this.dayOfWeek = dayOfWeek;
        this.mealType = mealType;
        this.mealTitle = mealTitle.trim();
        this.recipeId = recipeId;
        this.recipeTitleSnapshot = recipeTitleSnapshot == null ? null : recipeTitleSnapshot.trim();
        this.shoppingHandledAt = shoppingHandledAt;
        this.shoppingListId = shoppingListId;
    }

    public static PlannedMeal rehydrate(
            int dayOfWeek,
            MealType mealType,
            String mealTitle,
            java.util.UUID recipeId,
            String recipeTitleSnapshot,
            java.time.Instant shoppingHandledAt,
            java.util.UUID shoppingListId
    ) {
        return new PlannedMeal(
                dayOfWeek,
                mealType,
                mealTitle,
                recipeId,
                recipeTitleSnapshot,
                shoppingHandledAt,
                shoppingListId
        );
    }

    public static PlannedMeal rehydrate(
            int dayOfWeek,
            MealType mealType,
            java.util.UUID recipeId,
            String recipeTitleSnapshot
    ) {
        return new PlannedMeal(dayOfWeek, mealType, recipeTitleSnapshot, recipeId, recipeTitleSnapshot, null, null);
    }

    public static PlannedMeal rehydrate(
            int dayOfWeek,
            MealType mealType,
            String mealTitle,
            java.util.UUID recipeId,
            String recipeTitleSnapshot
    ) {
        return new PlannedMeal(dayOfWeek, mealType, mealTitle, recipeId, recipeTitleSnapshot, null, null);
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public MealType getMealType() {
        return mealType;
    }

    public String getMealTitle() {
        return mealTitle;
    }

    public java.util.UUID getRecipeId() {
        return recipeId;
    }

    public String getRecipeTitleSnapshot() {
        return recipeTitleSnapshot;
    }

    public java.time.Instant getShoppingHandledAt() {
        return shoppingHandledAt;
    }

    public java.util.UUID getShoppingListId() {
        return shoppingListId;
    }

    public boolean hasRecipe() {
        return recipeId != null;
    }

    public boolean isShoppingHandled() {
        return shoppingHandledAt != null && shoppingListId != null;
    }
}
