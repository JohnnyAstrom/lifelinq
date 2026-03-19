package app.lifelinq.features.meals.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeJpaRepository extends JpaRepository<RecipeEntity, UUID> {
    @EntityGraph(attributePaths = "ingredients")
    Optional<RecipeEntity> findByIdAndGroupId(UUID id, UUID groupId);

    @EntityGraph(attributePaths = "ingredients")
    List<RecipeEntity> findByGroupIdAndArchivedAtIsNullAndSavedInRecipesTrue(UUID groupId);

    @EntityGraph(attributePaths = "ingredients")
    List<RecipeEntity> findByGroupIdAndArchivedAtIsNotNullAndSavedInRecipesTrue(UUID groupId);

    @EntityGraph(attributePaths = "ingredients")
    List<RecipeEntity> findByGroupIdAndIdIn(UUID groupId, Collection<UUID> ids);

    @Modifying
    @Query("delete from RecipeEntity recipe where recipe.id = :recipeId and recipe.groupId = :groupId")
    void deleteByIdAndGroupId(@Param("recipeId") UUID recipeId, @Param("groupId") UUID groupId);
}
