package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.domain.HouseholdPreferenceSignal;
import app.lifelinq.features.meals.domain.HouseholdPreferenceSignalRepository;
import app.lifelinq.features.meals.domain.HouseholdPreferenceSignalTargetKind;
import app.lifelinq.features.meals.domain.HouseholdPreferenceSignalType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class JpaHouseholdPreferenceSignalRepositoryAdapter implements HouseholdPreferenceSignalRepository {
    private final HouseholdPreferenceSignalJpaRepository repository;

    public JpaHouseholdPreferenceSignalRepositoryAdapter(HouseholdPreferenceSignalJpaRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("repository must not be null");
        }
        this.repository = repository;
    }

    @Override
    public HouseholdPreferenceSignal save(HouseholdPreferenceSignal signal) {
        if (signal == null) {
            throw new IllegalArgumentException("signal must not be null");
        }
        HouseholdPreferenceSignalEntity entity = new HouseholdPreferenceSignalEntity(
                signal.getId(),
                signal.getGroupId(),
                signal.getTargetKind(),
                signal.getRecipeId(),
                signal.getMealIdentityKey(),
                signal.getSignalType(),
                signal.getCreatedAt(),
                signal.getUpdatedAt()
        );
        HouseholdPreferenceSignalEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<HouseholdPreferenceSignal> findByGroupId(UUID groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        return repository.findByGroupId(groupId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<HouseholdPreferenceSignal> findByRecipeTarget(
            UUID groupId,
            UUID recipeId,
            HouseholdPreferenceSignalType signalType
    ) {
        if (groupId == null || recipeId == null || signalType == null) {
            throw new IllegalArgumentException("groupId, recipeId, and signalType must not be null");
        }
        return repository.findByGroupIdAndTargetKindAndRecipeIdAndSignalType(
                groupId,
                HouseholdPreferenceSignalTargetKind.RECIPE,
                recipeId,
                signalType
        ).map(this::toDomain);
    }

    @Override
    public Optional<HouseholdPreferenceSignal> findByMealIdentityTarget(
            UUID groupId,
            String mealIdentityKey,
            HouseholdPreferenceSignalType signalType
    ) {
        if (groupId == null || mealIdentityKey == null || mealIdentityKey.isBlank() || signalType == null) {
            throw new IllegalArgumentException("groupId, mealIdentityKey, and signalType must not be null");
        }
        return repository.findByGroupIdAndTargetKindAndMealIdentityKeyAndSignalType(
                groupId,
                HouseholdPreferenceSignalTargetKind.MEAL_IDENTITY,
                mealIdentityKey.trim(),
                signalType
        ).map(this::toDomain);
    }

    @Override
    public void delete(HouseholdPreferenceSignal signal) {
        if (signal == null) {
            throw new IllegalArgumentException("signal must not be null");
        }
        repository.deleteById(signal.getId());
    }

    private HouseholdPreferenceSignal toDomain(HouseholdPreferenceSignalEntity entity) {
        return new HouseholdPreferenceSignal(
                entity.getId(),
                entity.getGroupId(),
                entity.getTargetKind(),
                entity.getRecipeId(),
                entity.getMealIdentityKey(),
                entity.getSignalType(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
