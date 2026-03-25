package app.lifelinq.features.meals.contract;

public record ContributorMealReferenceView(
        int dayOfWeek,
        String mealType,
        String mealTitle
) {
}
