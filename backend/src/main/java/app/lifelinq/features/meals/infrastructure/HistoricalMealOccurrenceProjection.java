package app.lifelinq.features.meals.infrastructure;

import java.util.UUID;

public interface HistoricalMealOccurrenceProjection {
    UUID getWeekPlanId();

    int getYear();

    int getIsoWeek();

    int getDayOfWeek();

    String getMealType();

    String getMealTitle();

    UUID getRecipeId();

    String getRecipeTitleSnapshot();
}
