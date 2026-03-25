package app.lifelinq.features.meals.domain;

public record WeekShoppingContributorMeal(
        int dayOfWeek,
        MealType mealType,
        String mealTitle
) {
    public WeekShoppingContributorMeal {
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new IllegalArgumentException("dayOfWeek must be between 1 and 7");
        }
        if (mealType == null) {
            throw new IllegalArgumentException("mealType must not be null");
        }
        if (mealTitle == null || mealTitle.isBlank()) {
            throw new IllegalArgumentException("mealTitle must not be blank");
        }
    }
}
