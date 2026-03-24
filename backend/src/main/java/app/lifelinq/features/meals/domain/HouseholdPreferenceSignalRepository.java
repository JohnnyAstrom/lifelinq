package app.lifelinq.features.meals.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HouseholdPreferenceSignalRepository {
    HouseholdPreferenceSignal save(HouseholdPreferenceSignal signal);

    List<HouseholdPreferenceSignal> findByGroupId(UUID groupId);

    Optional<HouseholdPreferenceSignal> findByRecipeTarget(
            UUID groupId,
            UUID recipeId,
            HouseholdPreferenceSignalType signalType
    );

    Optional<HouseholdPreferenceSignal> findByMealIdentityTarget(
            UUID groupId,
            String mealIdentityKey,
            HouseholdPreferenceSignalType signalType
    );

    void delete(HouseholdPreferenceSignal signal);
}
