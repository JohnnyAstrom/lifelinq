package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.domain.MealMemoryRepository;
import app.lifelinq.features.meals.domain.MealOccurrence;
import app.lifelinq.features.meals.domain.MealType;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class JpaMealMemoryRepositoryAdapter implements MealMemoryRepository {
    private final MealMemoryJpaRepository repository;

    public JpaMealMemoryRepositoryAdapter(MealMemoryJpaRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("repository must not be null");
        }
        this.repository = repository;
    }

    @Override
    public List<MealOccurrence> findHistoricalOccurrencesOnOrBefore(UUID groupId, int year, int isoWeek, int dayOfWeek) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        return repository.findHistoricalOccurrencesOnOrBefore(groupId, year, isoWeek, dayOfWeek).stream()
                .map(projection -> new MealOccurrence(
                        projection.getWeekPlanId(),
                        projection.getYear(),
                        projection.getIsoWeek(),
                        projection.getDayOfWeek(),
                        MealType.valueOf(projection.getMealType()),
                        LocalDate.of(projection.getYear(), 1, 4)
                                .with(WeekFields.ISO.weekOfWeekBasedYear(), projection.getIsoWeek())
                                .with(WeekFields.ISO.dayOfWeek(), projection.getDayOfWeek()),
                        projection.getMealTitle(),
                        projection.getRecipeId(),
                        projection.getRecipeTitleSnapshot()
                ))
                .toList();
    }
}
