package app.lifelinq.features.meals.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import app.lifelinq.features.meals.domain.HouseholdPreferenceSignal;
import app.lifelinq.features.meals.domain.HouseholdPreferenceSignalTargetKind;
import app.lifelinq.features.meals.domain.HouseholdPreferenceSignalType;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = MealsJpaTestApplication.class)
@ActiveProfiles("test")
class JpaHouseholdPreferenceSignalRepositoryAdapterTest {

    @Autowired
    private JpaHouseholdPreferenceSignalRepositoryAdapter repository;

    @Test
    void savesAndFindsRecipeAndMealIdentitySignalsByScopedTarget() {
        UUID groupId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        HouseholdPreferenceSignal recipeSignal = new HouseholdPreferenceSignal(
                UUID.randomUUID(),
                groupId,
                HouseholdPreferenceSignalTargetKind.RECIPE,
                recipeId,
                null,
                HouseholdPreferenceSignalType.PREFER,
                Instant.parse("2026-03-24T10:00:00Z"),
                Instant.parse("2026-03-24T10:00:00Z")
        );
        HouseholdPreferenceSignal identitySignal = new HouseholdPreferenceSignal(
                UUID.randomUUID(),
                groupId,
                HouseholdPreferenceSignalTargetKind.MEAL_IDENTITY,
                null,
                "title:tacos",
                HouseholdPreferenceSignalType.FALLBACK,
                Instant.parse("2026-03-24T10:05:00Z"),
                Instant.parse("2026-03-24T10:05:00Z")
        );

        repository.save(recipeSignal);
        repository.save(identitySignal);

        assertThat(repository.findByRecipeTarget(groupId, recipeId, HouseholdPreferenceSignalType.PREFER))
                .isPresent()
                .get()
                .extracting(HouseholdPreferenceSignal::getTargetKind)
                .isEqualTo(HouseholdPreferenceSignalTargetKind.RECIPE);
        assertThat(repository.findByMealIdentityTarget(groupId, "title:tacos", HouseholdPreferenceSignalType.FALLBACK))
                .isPresent();
        assertThat(repository.findByGroupId(groupId)).hasSize(2);
    }
}
