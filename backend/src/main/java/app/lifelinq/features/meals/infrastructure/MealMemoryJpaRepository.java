package app.lifelinq.features.meals.infrastructure;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface MealMemoryJpaRepository extends Repository<PlannedMealEntity, PlannedMealId> {

    @Query("""
            select
                wp.id as weekPlanId,
                wp.year as year,
                wp.isoWeek as isoWeek,
                pm.id.dayOfWeek as dayOfWeek,
                pm.id.mealType as mealType,
                pm.mealTitle as mealTitle,
                pm.recipeId as recipeId,
                pm.recipeTitleSnapshot as recipeTitleSnapshot
            from PlannedMealEntity pm
            join pm.weekPlan wp
            where wp.groupId = :groupId
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
            order by wp.year desc, wp.isoWeek desc, pm.id.dayOfWeek desc, pm.id.mealType asc
            """)
    List<HistoricalMealOccurrenceProjection> findHistoricalOccurrencesOnOrBefore(
            @Param("groupId") UUID groupId,
            @Param("year") int year,
            @Param("isoWeek") int isoWeek,
            @Param("dayOfWeek") int dayOfWeek
    );
}
