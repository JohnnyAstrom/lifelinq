package app.lifelinq.features.meals.contract;

import java.time.Instant;
import java.util.UUID;

public record HouseholdPreferenceSummaryView(
        UUID signalId,
        String targetKind,
        UUID recipeId,
        String mealIdentityKey,
        String signalType,
        Instant createdAt,
        Instant updatedAt
) {
}
