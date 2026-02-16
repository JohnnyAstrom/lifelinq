package app.lifelinq.features.meals.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeJpaRepository extends JpaRepository<RecipeEntity, UUID> {
    @EntityGraph(attributePaths = "ingredients")
    Optional<RecipeEntity> findByIdAndHouseholdId(UUID id, UUID householdId);

    @EntityGraph(attributePaths = "ingredients")
    List<RecipeEntity> findByHouseholdId(UUID householdId);

    @EntityGraph(attributePaths = "ingredients")
    List<RecipeEntity> findByHouseholdIdAndIdIn(UUID householdId, Collection<UUID> ids);
}
