package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.domain.WeekPlan;
import app.lifelinq.features.meals.domain.WeekPlanRepository;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Comparator;

public final class InMemoryWeekPlanRepository implements WeekPlanRepository {
    private final ConcurrentMap<UUID, WeekPlan> byId = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, UUID> byGroupWeek = new ConcurrentHashMap<>();

    @Override
    public WeekPlan save(WeekPlan weekPlan) {
        if (weekPlan == null) {
            throw new IllegalArgumentException("weekPlan must not be null");
        }
        byId.put(weekPlan.getId(), weekPlan);
        byGroupWeek.put(key(weekPlan.getGroupId(), weekPlan.getYear(), weekPlan.getIsoWeek()), weekPlan.getId());
        return weekPlan;
    }

    @Override
    public Optional<WeekPlan> findByGroupAndWeek(UUID groupId, int year, int isoWeek) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        UUID id = byGroupWeek.get(key(groupId, year, isoWeek));
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<WeekPlan> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public boolean existsCurrentOrFutureMealReferencingRecipe(UUID groupId, UUID recipeId, int year, int isoWeek) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        if (recipeId == null) {
            throw new IllegalArgumentException("recipeId must not be null");
        }
        return byId.values().stream()
                .filter(plan -> plan.getGroupId().equals(groupId))
                .filter(plan -> plan.getYear() > year || (plan.getYear() == year && plan.getIsoWeek() >= isoWeek))
                .flatMap(plan -> plan.getMeals().stream())
                .anyMatch(meal -> recipeId.equals(meal.getRecipeId()));
    }

    @Override
    public List<UUID> findRecentRecipeIdsOnOrBefore(UUID groupId, int year, int isoWeek, int dayOfWeek) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        return byId.values().stream()
                .filter(plan -> plan.getGroupId().equals(groupId))
                .filter(plan -> isPlanOnOrBefore(plan.getYear(), plan.getIsoWeek(), year, isoWeek))
                .flatMap(plan -> plan.getMeals().stream()
                        .filter(meal -> meal.getRecipeId() != null)
                        .filter(meal -> isMealOnOrBefore(
                                plan.getYear(),
                                plan.getIsoWeek(),
                                meal.getDayOfWeek(),
                                year,
                                isoWeek,
                                dayOfWeek
                        ))
                        .map(meal -> new RecentMealRecipe(plan.getYear(), plan.getIsoWeek(), meal.getDayOfWeek(), meal.getRecipeId())))
                .sorted(Comparator
                        .comparingInt(RecentMealRecipe::year).reversed()
                        .thenComparing(Comparator.comparingInt(RecentMealRecipe::isoWeek).reversed())
                        .thenComparing(Comparator.comparingInt(RecentMealRecipe::dayOfWeek).reversed()))
                .map(RecentMealRecipe::recipeId)
                .toList();
    }

    private boolean isPlanOnOrBefore(int planYear, int planIsoWeek, int year, int isoWeek) {
        return planYear < year || (planYear == year && planIsoWeek <= isoWeek);
    }

    private boolean isMealOnOrBefore(
            int planYear,
            int planIsoWeek,
            int mealDayOfWeek,
            int year,
            int isoWeek,
            int dayOfWeek
    ) {
        if (planYear < year) {
            return true;
        }
        if (planYear > year) {
            return false;
        }
        if (planIsoWeek < isoWeek) {
            return true;
        }
        if (planIsoWeek > isoWeek) {
            return false;
        }
        return mealDayOfWeek <= dayOfWeek;
    }

    private record RecentMealRecipe(int year, int isoWeek, int dayOfWeek, UUID recipeId) {
    }

    private String key(UUID groupId, int year, int isoWeek) {
        return groupId + ":" + year + ":" + isoWeek;
    }
}
