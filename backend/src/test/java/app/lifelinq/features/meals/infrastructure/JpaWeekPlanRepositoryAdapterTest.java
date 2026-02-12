package app.lifelinq.features.meals.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.meals.domain.RecipeRef;
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
        UUID householdId = UUID.randomUUID();
        UUID weekPlanId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        WeekPlan plan = new WeekPlan(weekPlanId, householdId, 2025, 10, createdAt);
        UUID recipeId = UUID.randomUUID();
        plan.addOrReplaceMeal(1, new RecipeRef(recipeId, "Pasta"));

        repository.save(plan);

        Optional<WeekPlan> loaded = repository.findByHouseholdAndWeek(householdId, 2025, 10);
        assertTrue(loaded.isPresent());
        WeekPlan loadedPlan = loaded.get();
        assertEquals(weekPlanId, loadedPlan.getId());
        assertEquals(householdId, loadedPlan.getHouseholdId());
        assertEquals(2025, loadedPlan.getYear());
        assertEquals(10, loadedPlan.getIsoWeek());
        long diffNanos = Math.abs(Duration.between(createdAt, loadedPlan.getCreatedAt()).toNanos());
        assertTrue(diffNanos <= 1_000);
        assertEquals(1, loadedPlan.getMeals().size());
        assertEquals(1, loadedPlan.getMeals().get(0).getDayOfWeek());
        assertEquals(recipeId, loadedPlan.getMeals().get(0).getRecipeRef().recipeId());
    }

    @Test
    void replacesMealForSameDay() {
        UUID householdId = UUID.randomUUID();
        UUID weekPlanId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        WeekPlan plan = new WeekPlan(weekPlanId, householdId, 2025, 12, createdAt);
        plan.addOrReplaceMeal(2, new RecipeRef(UUID.randomUUID(), "Soup"));
        repository.save(plan);

        WeekPlan reloaded = repository.findByHouseholdAndWeek(householdId, 2025, 12).orElseThrow();
        UUID newRecipeId = UUID.randomUUID();
        reloaded.addOrReplaceMeal(2, new RecipeRef(newRecipeId, "Tacos"));
        repository.save(reloaded);

        WeekPlan updated = repository.findByHouseholdAndWeek(householdId, 2025, 12).orElseThrow();
        assertEquals(1, updated.getMeals().size());
        assertEquals("Tacos", updated.getMeals().get(0).getRecipeRef().title());
        assertEquals(newRecipeId, updated.getMeals().get(0).getRecipeRef().recipeId());
    }

    @Test
    void removesMeal() {
        UUID householdId = UUID.randomUUID();
        UUID weekPlanId = UUID.randomUUID();
        WeekPlan plan = new WeekPlan(weekPlanId, householdId, 2025, 15, Instant.now());
        plan.addOrReplaceMeal(5, new RecipeRef(UUID.randomUUID(), "Salad"));
        repository.save(plan);

        WeekPlan reloaded = repository.findByHouseholdAndWeek(householdId, 2025, 15).orElseThrow();
        reloaded.removeMeal(5);
        repository.save(reloaded);

        WeekPlan updated = repository.findByHouseholdAndWeek(householdId, 2025, 15).orElseThrow();
        assertTrue(updated.getMeals().isEmpty());
    }
}
