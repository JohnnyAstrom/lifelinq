package app.lifelinq.features.meals.infrastructure;

import java.util.Optional;
import java.util.List;
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
            where wp.groupId = :groupId
              and pm.recipeId = :recipeId
              and (wp.year > :year or (wp.year = :year and wp.isoWeek >= :isoWeek))
            """)
    boolean existsCurrentOrFutureMealReferencingRecipe(
            @Param("groupId") UUID groupId,
            @Param("recipeId") UUID recipeId,
            @Param("year") int year,
            @Param("isoWeek") int isoWeek
    );

    @Query("""
            select pm.recipeId
            from PlannedMealEntity pm
            join pm.weekPlan wp
            where wp.groupId = :groupId
              and pm.recipeId is not null
              and (
                    wp.year < :year
                    or (
                        wp.year = :year
                        and (
                            wp.isoWeek < :isoWeek
                            or (wp.isoWeek = :isoWeek and pm.id.dayOfWeek <= :dayOfWeek)
                        )
                    )
              )
            order by wp.year desc, wp.isoWeek desc, pm.id.dayOfWeek desc
            """)
    List<UUID> findRecentRecipeIdsOnOrBefore(
            @Param("groupId") UUID groupId,
            @Param("year") int year,
            @Param("isoWeek") int isoWeek,
            @Param("dayOfWeek") int dayOfWeek
    );
}
