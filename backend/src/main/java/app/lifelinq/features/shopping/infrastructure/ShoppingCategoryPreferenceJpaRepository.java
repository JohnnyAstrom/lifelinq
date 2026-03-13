package app.lifelinq.features.shopping.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface ShoppingCategoryPreferenceJpaRepository extends JpaRepository<ShoppingCategoryPreferenceEntity, UUID> {
    List<ShoppingCategoryPreferenceEntity> findByGroupId(UUID groupId);

    Optional<ShoppingCategoryPreferenceEntity> findByGroupIdAndListTypeAndNormalizedTitle(
            UUID groupId,
            String listType,
            String normalizedTitle
    );

    @Modifying
    @Transactional
    void deleteByGroupIdAndListTypeAndNormalizedTitle(
            UUID groupId,
            String listType,
            String normalizedTitle
    );
}
