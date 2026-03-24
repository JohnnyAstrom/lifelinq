package app.lifelinq.features.meals.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import app.lifelinq.features.meals.domain.MealMemoryRepository;
import app.lifelinq.features.meals.domain.MealOccurrence;
import app.lifelinq.features.meals.domain.MealType;
import app.lifelinq.features.meals.domain.WeekPlan;
import app.lifelinq.features.meals.domain.WeekPlanRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = MealsJpaTestApplication.class)
@ActiveProfiles("test")
class JpaMealMemoryRepositoryAdapterTest {

    @Autowired
    private WeekPlanRepository weekPlanRepository;

    @Autowired
    private MealMemoryRepository mealMemoryRepository;

    @Test
    void projectsHistoricalMealOccurrencesFromWeekPlans() {
        UUID groupId = UUID.randomUUID();
        WeekPlan week10 = new WeekPlan(UUID.randomUUID(), groupId, 2026, 10, Instant.parse("2026-03-01T10:00:00Z"));
        week10.addOrReplaceMeal(2, MealType.DINNER, "Tacos", null, null);
        WeekPlan week12 = new WeekPlan(UUID.randomUUID(), groupId, 2026, 12, Instant.parse("2026-03-15T10:00:00Z"));
        UUID recipeId = UUID.randomUUID();
        week12.addOrReplaceMeal(4, MealType.DINNER, "Soup", recipeId, "Soup");

        weekPlanRepository.save(week10);
        weekPlanRepository.save(week12);

        List<MealOccurrence> occurrences = mealMemoryRepository.findHistoricalOccurrencesOnOrBefore(groupId, 2026, 12, 5);

        assertThat(occurrences).hasSize(2);
        assertThat(occurrences.get(0).mealTitle()).isEqualTo("Soup");
        assertThat(occurrences.get(0).recipeId()).isEqualTo(recipeId);
        assertThat(occurrences.get(1).mealTitle()).isEqualTo("Tacos");
        assertThat(occurrences.get(1).recipeId()).isNull();
    }
}
