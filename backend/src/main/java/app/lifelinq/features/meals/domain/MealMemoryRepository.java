package app.lifelinq.features.meals.domain;

import java.util.List;
import java.util.UUID;

public interface MealMemoryRepository {
    List<MealOccurrence> findHistoricalOccurrencesOnOrBefore(UUID groupId, int year, int isoWeek, int dayOfWeek);
}
