package app.lifelinq.features.shopping.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShoppingCategoryPreferenceRepository {
    ShoppingCategoryPreference save(ShoppingCategoryPreference preference);

    List<ShoppingCategoryPreference> findByGroupId(UUID groupId);

    Optional<ShoppingCategoryPreference> findByGroupIdAndListTypeAndNormalizedTitle(
            UUID groupId,
            ShoppingListType listType,
            String normalizedTitle
    );

    void deleteByGroupIdAndListTypeAndNormalizedTitle(
            UUID groupId,
            ShoppingListType listType,
            String normalizedTitle
    );
}
