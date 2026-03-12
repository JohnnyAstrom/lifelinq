package app.lifelinq.features.shopping.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingCategoryPreferenceJpaRepository extends JpaRepository<ShoppingCategoryPreferenceEntity, UUID> {
    List<ShoppingCategoryPreferenceEntity> findByGroupId(UUID groupId);

    Optional<ShoppingCategoryPreferenceEntity> findByGroupIdAndListTypeAndNormalizedTitle(
            UUID groupId,
            String listType,
            String normalizedTitle
    );
}
