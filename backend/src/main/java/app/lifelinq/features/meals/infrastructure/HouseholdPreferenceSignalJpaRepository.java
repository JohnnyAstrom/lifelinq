package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.domain.HouseholdPreferenceSignalTargetKind;
import app.lifelinq.features.meals.domain.HouseholdPreferenceSignalType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HouseholdPreferenceSignalJpaRepository extends JpaRepository<HouseholdPreferenceSignalEntity, UUID> {
    List<HouseholdPreferenceSignalEntity> findByGroupId(UUID groupId);

    Optional<HouseholdPreferenceSignalEntity> findByGroupIdAndTargetKindAndRecipeIdAndSignalType(
            UUID groupId,
            HouseholdPreferenceSignalTargetKind targetKind,
            UUID recipeId,
            HouseholdPreferenceSignalType signalType
    );

    Optional<HouseholdPreferenceSignalEntity> findByGroupIdAndTargetKindAndMealIdentityKeyAndSignalType(
            UUID groupId,
            HouseholdPreferenceSignalTargetKind targetKind,
            String mealIdentityKey,
            HouseholdPreferenceSignalType signalType
    );
}
