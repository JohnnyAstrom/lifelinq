package app.lifelinq.features.meals.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import app.lifelinq.features.meals.domain.Ingredient;
import app.lifelinq.features.meals.domain.Recipe;
import app.lifelinq.features.shopping.domain.ShoppingUnit;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = MealsJpaTestApplication.class)
@ActiveProfiles("test")
class JpaRecipeRepositoryAdapterTest {

    @Autowired
    private JpaRecipeRepositoryAdapter repository;

    @Test
    void savesAndLoadsRecipeWithIngredientsOrderedByPositionThenId() {
        UUID householdId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        Recipe recipe = new Recipe(
                recipeId,
                householdId,
                "Soup",
                Instant.parse("2026-02-01T10:00:00Z"),
                List.of(
                        new Ingredient(
                                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                                "Tomato",
                                null,
                                null,
                                2
                        ),
                        new Ingredient(
                                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                                "Olive Oil",
                                new BigDecimal("1.5"),
                                ShoppingUnit.DL,
                                1
                        )
                )
        );

        repository.save(recipe);
        Optional<Recipe> loaded = repository.findByIdAndHouseholdId(recipeId, householdId);

        assertThat(loaded).isPresent();
        assertThat(loaded.get().getIngredients())
                .extracting(Ingredient::getName)
                .containsExactly("Olive Oil", "Tomato");
        assertThat(loaded.get().getIngredients().get(0).getQuantity()).isEqualByComparingTo(new BigDecimal("1.5"));
        assertThat(loaded.get().getIngredients().get(0).getUnit()).isEqualTo(ShoppingUnit.DL);
    }

    @Test
    void findByHouseholdIdAndIdsIsScopedByHousehold() {
        UUID householdA = UUID.randomUUID();
        UUID householdB = UUID.randomUUID();
        UUID recipeA = UUID.randomUUID();
        UUID recipeB = UUID.randomUUID();

        repository.save(new Recipe(
                recipeA,
                householdA,
                "A",
                Instant.parse("2026-02-01T10:00:00Z"),
                List.of()
        ));
        repository.save(new Recipe(
                recipeB,
                householdB,
                "B",
                Instant.parse("2026-02-01T10:00:00Z"),
                List.of()
        ));

        List<Recipe> result = repository.findByHouseholdIdAndIds(householdA, Set.of(recipeA, recipeB));

        assertThat(result).extracting(Recipe::getId).containsExactly(recipeA);
    }
}
