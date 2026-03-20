package app.lifelinq.features.meals.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.meals.domain.WeekPlan;
import app.lifelinq.features.meals.domain.WeekPlanRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = MealsJpaTestApplication.class)
@ActiveProfiles("test")
class JpaWeekPlanRepositoryAdapterTest {

    @Autowired
    private WeekPlanRepository repository;

    @Test
    void savesAndLoadsWeekPlanRoundTrip() {
        UUID groupId = UUID.randomUUID();
        UUID weekPlanId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        WeekPlan plan = new WeekPlan(weekPlanId, groupId, 2025, 10, createdAt);
        UUID recipeId = UUID.randomUUID();
        plan.addOrReplaceMeal(1, app.lifelinq.features.meals.domain.MealType.DINNER, recipeId, "Soup");

        repository.save(plan);

        Optional<WeekPlan> loaded = repository.findByGroupAndWeek(groupId, 2025, 10);
        assertTrue(loaded.isPresent());
        WeekPlan loadedPlan = loaded.get();
        assertEquals(weekPlanId, loadedPlan.getId());
        assertEquals(groupId, loadedPlan.getGroupId());
        assertEquals(2025, loadedPlan.getYear());
        assertEquals(10, loadedPlan.getIsoWeek());
        long diffNanos = Math.abs(Duration.between(createdAt, loadedPlan.getCreatedAt()).toNanos());
        assertTrue(diffNanos <= 1_000);
        assertEquals(1, loadedPlan.getMeals().size());
        assertEquals(1, loadedPlan.getMeals().get(0).getDayOfWeek());
        assertEquals("Soup", loadedPlan.getMeals().get(0).getMealTitle());
        assertEquals(recipeId, loadedPlan.getMeals().get(0).getRecipeId());
        assertEquals("Soup", loadedPlan.getMeals().get(0).getRecipeTitleSnapshot());
    }

    @Test
    void replacesMealForSameDay() {
        UUID groupId = UUID.randomUUID();
        UUID weekPlanId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        WeekPlan plan = new WeekPlan(weekPlanId, groupId, 2025, 12, createdAt);
        plan.addOrReplaceMeal(2, app.lifelinq.features.meals.domain.MealType.DINNER, UUID.randomUUID(), "Original");
        repository.save(plan);

        WeekPlan reloaded = repository.findByGroupAndWeek(groupId, 2025, 12).orElseThrow();
        UUID newRecipeId = UUID.randomUUID();
        reloaded.addOrReplaceMeal(2, app.lifelinq.features.meals.domain.MealType.DINNER, newRecipeId, "Updated");
        repository.save(reloaded);

        WeekPlan updated = repository.findByGroupAndWeek(groupId, 2025, 12).orElseThrow();
        assertEquals(1, updated.getMeals().size());
        assertEquals("Updated", updated.getMeals().get(0).getMealTitle());
        assertEquals(newRecipeId, updated.getMeals().get(0).getRecipeId());
        assertEquals("Updated", updated.getMeals().get(0).getRecipeTitleSnapshot());
    }

    @Test
    void savesAndLoadsLightweightMealWithoutRecipe() {
        UUID groupId = UUID.randomUUID();
        UUID weekPlanId = UUID.randomUUID();
        WeekPlan plan = new WeekPlan(weekPlanId, groupId, 2025, 13, Instant.now());
        plan.addOrReplaceMeal(3, app.lifelinq.features.meals.domain.MealType.LUNCH, "Leftovers", null, null);

        repository.save(plan);

        WeekPlan loaded = repository.findByGroupAndWeek(groupId, 2025, 13).orElseThrow();
        assertEquals(1, loaded.getMeals().size());
        assertEquals("Leftovers", loaded.getMeals().get(0).getMealTitle());
        assertEquals(null, loaded.getMeals().get(0).getRecipeId());
        assertEquals(null, loaded.getMeals().get(0).getRecipeTitleSnapshot());
    }

    @Test
    void savesAndLoadsShoppingHandledSnapshot() {
        UUID groupId = UUID.randomUUID();
        UUID weekPlanId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        UUID shoppingListId = UUID.randomUUID();
        Instant handledAt = Instant.parse("2026-03-20T10:15:00Z");
        WeekPlan plan = new WeekPlan(weekPlanId, groupId, 2025, 14, Instant.now());
        plan.addOrReplaceMeal(
                4,
                app.lifelinq.features.meals.domain.MealType.DINNER,
                "Soup",
                recipeId,
                "Soup",
                handledAt,
                shoppingListId
        );

        repository.save(plan);

        WeekPlan loaded = repository.findByGroupAndWeek(groupId, 2025, 14).orElseThrow();
        assertEquals(1, loaded.getMeals().size());
        assertEquals(handledAt, loaded.getMeals().get(0).getShoppingHandledAt());
        assertEquals(shoppingListId, loaded.getMeals().get(0).getShoppingListId());
    }

    @Test
    void removesMeal() {
        UUID groupId = UUID.randomUUID();
        UUID weekPlanId = UUID.randomUUID();
        WeekPlan plan = new WeekPlan(weekPlanId, groupId, 2025, 15, Instant.now());
        plan.addOrReplaceMeal(5, app.lifelinq.features.meals.domain.MealType.DINNER, UUID.randomUUID(), "Remove me");
        repository.save(plan);

        WeekPlan reloaded = repository.findByGroupAndWeek(groupId, 2025, 15).orElseThrow();
        reloaded.removeMeal(5, app.lifelinq.features.meals.domain.MealType.DINNER);
        repository.save(reloaded);

        WeekPlan updated = repository.findByGroupAndWeek(groupId, 2025, 15).orElseThrow();
        assertTrue(updated.getMeals().isEmpty());
    }
}
