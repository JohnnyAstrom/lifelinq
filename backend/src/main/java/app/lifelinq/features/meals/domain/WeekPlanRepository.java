package app.lifelinq.features.meals.domain;

import java.util.Optional;
import java.util.UUID;

public interface WeekPlanRepository {
    WeekPlan save(WeekPlan weekPlan);

    Optional<WeekPlan> findByGroupAndWeek(UUID groupId, int year, int isoWeek);

    Optional<WeekPlan> findById(UUID id);
}
