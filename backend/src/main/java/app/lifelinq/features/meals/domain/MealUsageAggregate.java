package app.lifelinq.features.meals.domain;

import java.time.LocalDate;
import java.util.Set;

public record MealUsageAggregate(
        MealIdentity identity,
        int totalOccurrences,
        int recentOccurrences,
        int distinctWeeks,
        LocalDate lastPlannedDate,
        Set<MealType> usedMealTypes,
        boolean recent,
        boolean frequent,
        boolean familiar,
        boolean fallback,
        boolean preferenceFit,
        boolean deprioritized,
        boolean makeSoon
) {
    public MealUsageAggregate {
        if (identity == null) {
            throw new IllegalArgumentException("identity must not be null");
        }
        if (totalOccurrences < 0 || recentOccurrences < 0 || distinctWeeks < 0) {
            throw new IllegalArgumentException("usage counts must not be negative");
        }
        if (lastPlannedDate == null) {
            throw new IllegalArgumentException("lastPlannedDate must not be null");
        }
        if (usedMealTypes == null) {
            throw new IllegalArgumentException("usedMealTypes must not be null");
        }
        usedMealTypes = Set.copyOf(usedMealTypes);
    }
}
