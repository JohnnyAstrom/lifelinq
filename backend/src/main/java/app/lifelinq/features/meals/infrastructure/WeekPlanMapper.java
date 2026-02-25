package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.domain.PlannedMeal;
import app.lifelinq.features.meals.domain.WeekPlan;
import app.lifelinq.features.meals.domain.MealType;
import java.util.HashMap;
import java.util.Map;

final class WeekPlanMapper {

    WeekPlanEntity toEntity(WeekPlan weekPlan) {
        WeekPlanEntity entity = new WeekPlanEntity(
                weekPlan.getId(),
                weekPlan.getGroupId(),
                weekPlan.getYear(),
                weekPlan.getIsoWeek(),
                weekPlan.getCreatedAt()
        );
        for (PlannedMeal meal : weekPlan.getMeals()) {
            entity.getMeals().add(toEntity(meal, entity));
        }
        return entity;
    }

    WeekPlan toDomain(WeekPlanEntity entity) {
        Map<WeekPlan.DaySlot, PlannedMeal> mealsByDay = new HashMap<>();
        for (PlannedMealEntity meal : entity.getMeals()) {
            MealType mealType = MealType.valueOf(meal.getMealType());
            mealsByDay.put(
                    new WeekPlan.DaySlot(meal.getDayOfWeek(), mealType),
                    PlannedMeal.rehydrate(
                            meal.getDayOfWeek(),
                            mealType,
                            meal.getRecipeId()
                    )
            );
        }
        return new WeekPlan(
                entity.getId(),
                entity.getGroupId(),
                entity.getYear(),
                entity.getIsoWeek(),
                entity.getCreatedAt(),
                mealsByDay
        );
    }

    PlannedMealEntity toEntity(PlannedMeal meal, WeekPlanEntity weekPlan) {
        return new PlannedMealEntity(
                new PlannedMealId(weekPlan.getId(), meal.getDayOfWeek(), meal.getMealType().name()),
                weekPlan,
                meal.getRecipeId()
        );
    }
}
