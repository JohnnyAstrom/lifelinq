package app.lifelinq.features.meals.domain;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface WeekPlanRepository {
    WeekPlan save(WeekPlan weekPlan);

    Optional<WeekPlan> findByGroupAndWeek(UUID groupId, int year, int isoWeek);

    Optional<WeekPlan> findById(UUID id);

    boolean existsCurrentOrFutureMealReferencingRecipe(UUID groupId, UUID recipeId, int year, int isoWeek);

    List<UUID> findRecentRecipeIdsOnOrBefore(UUID groupId, int year, int isoWeek, int dayOfWeek);
}
