package app.lifelinq.features.meals.infrastructure;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WeekPlanJpaRepository extends JpaRepository<WeekPlanEntity, UUID> {

    @EntityGraph(attributePaths = "meals")
    Optional<WeekPlanEntity> findByGroupIdAndYearAndIsoWeek(UUID groupId, int year, int isoWeek);

    @Override
    @EntityGraph(attributePaths = "meals")
    Optional<WeekPlanEntity> findById(UUID id);

    @Query("""
            select case when count(pm) > 0 then true else false end
            from PlannedMealEntity pm
            join pm.weekPlan wp
            where wp.groupId = :groupId and pm.recipeId = :recipeId
            """)
    boolean existsMealReferencingRecipe(@Param("groupId") UUID groupId, @Param("recipeId") UUID recipeId);
}
