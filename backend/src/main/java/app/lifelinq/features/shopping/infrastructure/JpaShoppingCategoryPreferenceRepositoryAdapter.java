package app.lifelinq.features.shopping.infrastructure;

import app.lifelinq.features.shopping.domain.ShoppingCategory;
import app.lifelinq.features.shopping.domain.ShoppingCategoryPreference;
import app.lifelinq.features.shopping.domain.ShoppingCategoryPreferenceRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class JpaShoppingCategoryPreferenceRepositoryAdapter implements ShoppingCategoryPreferenceRepository {
    private final ShoppingCategoryPreferenceJpaRepository repository;

    public JpaShoppingCategoryPreferenceRepositoryAdapter(ShoppingCategoryPreferenceJpaRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("repository must not be null");
        }
        this.repository = repository;
    }

    @Override
    public ShoppingCategoryPreference save(ShoppingCategoryPreference preference) {
        if (preference == null) {
            throw new IllegalArgumentException("preference must not be null");
        }
        ShoppingCategoryPreferenceEntity entity = repository
                .findByGroupIdAndNormalizedTitle(preference.groupId(), preference.normalizedTitle())
                .map(existing -> {
                    existing.updatePreferredCategory(preference.preferredCategory().key(), preference.updatedAt());
                    return existing;
                })
                .orElseGet(() -> new ShoppingCategoryPreferenceEntity(
                        UUID.randomUUID(),
                        preference.groupId(),
                        preference.normalizedTitle(),
                        preference.preferredCategory().key(),
                        preference.updatedAt()
                ));
        ShoppingCategoryPreferenceEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<ShoppingCategoryPreference> findByGroupId(UUID groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        List<ShoppingCategoryPreference> result = new ArrayList<>();
        for (ShoppingCategoryPreferenceEntity entity : repository.findByGroupId(groupId)) {
            result.add(toDomain(entity));
        }
        return result;
    }

    @Override
    public Optional<ShoppingCategoryPreference> findByGroupIdAndNormalizedTitle(UUID groupId, String normalizedTitle) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        if (normalizedTitle == null || normalizedTitle.isBlank()) {
            throw new IllegalArgumentException("normalizedTitle must not be blank");
        }
        return repository.findByGroupIdAndNormalizedTitle(groupId, normalizedTitle).map(this::toDomain);
    }

    private ShoppingCategoryPreference toDomain(ShoppingCategoryPreferenceEntity entity) {
        return new ShoppingCategoryPreference(
                entity.getGroupId(),
                entity.getNormalizedTitle(),
                ShoppingCategory.fromKey(entity.getPreferredCategory()),
                entity.getUpdatedAt()
        );
    }
}
