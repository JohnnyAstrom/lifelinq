package app.lifelinq.features.meals.infrastructure;

import java.util.UUID;

public interface RecentPlannedMealProjection {
    int getYear();

    int getIsoWeek();

    int getDayOfWeek();

    String getMealType();

    String getMealTitle();

    UUID getRecipeId();

    String getRecipeTitleSnapshot();
}
