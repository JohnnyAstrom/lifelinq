package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.domain.PlannedMeal;
import app.lifelinq.features.meals.domain.RecipeRef;
import app.lifelinq.features.meals.domain.WeekPlan;
import java.util.HashMap;
import java.util.Map;

final class WeekPlanMapper {

    WeekPlanEntity toEntity(WeekPlan weekPlan) {
        WeekPlanEntity entity = new WeekPlanEntity(
                weekPlan.getId(),
                weekPlan.getHouseholdId(),
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
        Map<Integer, PlannedMeal> mealsByDay = new HashMap<>();
        for (PlannedMealEntity meal : entity.getMeals()) {
            mealsByDay.put(
                    meal.getDayOfWeek(),
                    PlannedMeal.rehydrate(
                            meal.getDayOfWeek(),
                            new RecipeRef(meal.getRecipeId(), meal.getRecipeTitle())
                    )
            );
        }
        return new WeekPlan(
                entity.getId(),
                entity.getHouseholdId(),
                entity.getYear(),
                entity.getIsoWeek(),
                entity.getCreatedAt(),
                mealsByDay
        );
    }

    PlannedMealEntity toEntity(PlannedMeal meal, WeekPlanEntity weekPlan) {
        return new PlannedMealEntity(
                new PlannedMealId(weekPlan.getId(), meal.getDayOfWeek()),
                weekPlan,
                meal.getRecipeRef().recipeId(),
                meal.getRecipeRef().title()
        );
    }
}
