package app.lifelinq.features.shopping.infrastructure;

import app.lifelinq.features.shopping.domain.ShoppingCategory;
import app.lifelinq.features.shopping.domain.ShoppingCategoryPreference;
import app.lifelinq.features.shopping.domain.ShoppingCategoryPreferenceRepository;
import app.lifelinq.features.shopping.domain.ShoppingListType;
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
                .findByGroupIdAndListTypeAndNormalizedTitle(
                        preference.groupId(),
                        preference.listType().key(),
                        preference.normalizedTitle()
                )
                .map(existing -> {
                    existing.updatePreferredCategory(preference.preferredCategory().key(), preference.updatedAt());
                    return existing;
                })
                .orElseGet(() -> new ShoppingCategoryPreferenceEntity(
                        UUID.randomUUID(),
                        preference.groupId(),
                        preference.listType().key(),
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
    public Optional<ShoppingCategoryPreference> findByGroupIdAndListTypeAndNormalizedTitle(
            UUID groupId,
            ShoppingListType listType,
            String normalizedTitle
    ) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        if (listType == null) {
            throw new IllegalArgumentException("listType must not be null");
        }
        if (normalizedTitle == null || normalizedTitle.isBlank()) {
            throw new IllegalArgumentException("normalizedTitle must not be blank");
        }
        return repository.findByGroupIdAndListTypeAndNormalizedTitle(groupId, listType.key(), normalizedTitle)
                .map(this::toDomain);
    }

    @Override
    public void deleteByGroupIdAndListTypeAndNormalizedTitle(
            UUID groupId,
            ShoppingListType listType,
            String normalizedTitle
    ) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        if (listType == null) {
            throw new IllegalArgumentException("listType must not be null");
        }
        if (normalizedTitle == null || normalizedTitle.isBlank()) {
            throw new IllegalArgumentException("normalizedTitle must not be blank");
        }
        repository.deleteByGroupIdAndListTypeAndNormalizedTitle(groupId, listType.key(), normalizedTitle);
    }

    private ShoppingCategoryPreference toDomain(ShoppingCategoryPreferenceEntity entity) {
        return new ShoppingCategoryPreference(
                entity.getGroupId(),
                ShoppingListType.fromKey(entity.getListType()),
                entity.getNormalizedTitle(),
                ShoppingCategory.fromKey(entity.getPreferredCategory()),
                entity.getUpdatedAt()
        );
    }
}
