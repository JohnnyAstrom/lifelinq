package app.lifelinq.features.meals.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeJpaRepository extends JpaRepository<RecipeEntity, UUID> {
    @EntityGraph(attributePaths = "ingredients")
    Optional<RecipeEntity> findByIdAndGroupId(UUID id, UUID groupId);

    @EntityGraph(attributePaths = "ingredients")
    List<RecipeEntity> findByGroupId(UUID groupId);

    @EntityGraph(attributePaths = "ingredients")
    List<RecipeEntity> findByGroupIdAndIdIn(UUID groupId, Collection<UUID> ids);
}
