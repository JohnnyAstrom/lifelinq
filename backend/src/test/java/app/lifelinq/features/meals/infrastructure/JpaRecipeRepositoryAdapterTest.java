package app.lifelinq.features.meals.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import app.lifelinq.features.meals.domain.Ingredient;
import app.lifelinq.features.meals.domain.IngredientUnit;
import app.lifelinq.features.meals.domain.Recipe;
import app.lifelinq.features.meals.domain.RecipeOriginKind;
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
        UUID groupId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        Recipe recipe = new Recipe(
                recipeId,
                groupId,
                "Soup",
                "Cookbook",
                "https://example.com/soup",
                RecipeOriginKind.URL_IMPORT,
                "4 servings",
                "Comfort food",
                "Stir and simmer",
                Instant.parse("2026-02-01T10:00:00Z"),
                Instant.parse("2026-02-02T09:00:00Z"),
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
                                IngredientUnit.DL,
                                1
                        )
                )
        );

        repository.save(recipe);
        Optional<Recipe> loaded = repository.findByIdAndGroupId(recipeId, groupId);

        assertThat(loaded).isPresent();
        assertThat(loaded.get().getIngredients())
                .extracting(Ingredient::getName)
                .containsExactly("Olive Oil", "Tomato");
        assertThat(loaded.get().getSourceName()).isEqualTo("Cookbook");
        assertThat(loaded.get().getSourceUrl()).isEqualTo("https://example.com/soup");
        assertThat(loaded.get().getOriginKind()).isEqualTo(RecipeOriginKind.URL_IMPORT);
        assertThat(loaded.get().getServings()).isEqualTo("4 servings");
        assertThat(loaded.get().getShortNote()).isEqualTo("Comfort food");
        assertThat(loaded.get().getInstructions()).isEqualTo("Stir and simmer");
        assertThat(loaded.get().getUpdatedAt()).isEqualTo(Instant.parse("2026-02-02T09:00:00Z"));
        assertThat(loaded.get().getArchivedAt()).isNull();
        assertThat(loaded.get().getIngredients().get(0).getQuantity()).isEqualByComparingTo(new BigDecimal("1.5"));
        assertThat(loaded.get().getIngredients().get(0).getUnit()).isEqualTo(IngredientUnit.DL);
        assertThat(loaded.get().getIngredients().get(0).getRawText()).isNull();
    }

    @Test
    void savesAndLoadsIngredientRawText() {
        UUID groupId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        Recipe recipe = new Recipe(
                recipeId,
                groupId,
                "Imported Soup",
                Instant.parse("2026-02-01T10:00:00Z"),
                List.of(
                        new Ingredient(
                                UUID.randomUUID(),
                                "Mushrooms",
                                "400g mushrooms",
                                new BigDecimal("400"),
                                IngredientUnit.G,
                                1
                        )
                )
        );

        repository.save(recipe);
        Optional<Recipe> loaded = repository.findByIdAndGroupId(recipeId, groupId);

        assertThat(loaded).isPresent();
        assertThat(loaded.get().getIngredients()).hasSize(1);
        assertThat(loaded.get().getIngredients().get(0).getRawText()).isEqualTo("400g mushrooms");
    }

    @Test
    void findActiveByGroupIdExcludesArchivedRecipes() {
        UUID groupId = UUID.randomUUID();

        repository.save(new Recipe(
                UUID.randomUUID(),
                groupId,
                "Active",
                "Notebook",
                null,
                RecipeOriginKind.MANUAL,
                null,
                null,
                Instant.parse("2026-02-01T10:00:00Z"),
                Instant.parse("2026-02-01T10:00:00Z"),
                null,
                true,
                List.of()
        ));
        repository.save(new Recipe(
                UUID.randomUUID(),
                groupId,
                "Archived",
                "Notebook",
                null,
                RecipeOriginKind.MANUAL,
                null,
                null,
                Instant.parse("2026-02-01T10:00:00Z"),
                Instant.parse("2026-02-03T10:00:00Z"),
                Instant.parse("2026-02-03T10:00:00Z"),
                true,
                List.of()
        ));

        List<Recipe> result = repository.findActiveByGroupId(groupId);

        assertThat(result).extracting(Recipe::getName).containsExactly("Active");
    }

    @Test
    void findArchivedByGroupIdReturnsOnlyArchivedRecipes() {
        UUID groupId = UUID.randomUUID();

        repository.save(new Recipe(
                UUID.randomUUID(),
                groupId,
                "Active",
                Instant.parse("2026-02-01T10:00:00Z"),
                List.of()
        ));
        repository.save(new Recipe(
                UUID.randomUUID(),
                groupId,
                "Archived",
                null,
                null,
                RecipeOriginKind.MANUAL,
                null,
                null,
                Instant.parse("2026-02-01T10:00:00Z"),
                Instant.parse("2026-02-03T10:00:00Z"),
                Instant.parse("2026-02-03T10:00:00Z"),
                true,
                List.of()
        ));

        List<Recipe> result = repository.findArchivedByGroupId(groupId);

        assertThat(result).extracting(Recipe::getName).containsExactly("Archived");
    }

    @Test
    void findByGroupIdAndIdsIsScopedByGroup() {
        UUID groupA = UUID.randomUUID();
        UUID groupB = UUID.randomUUID();
        UUID recipeA = UUID.randomUUID();
        UUID recipeB = UUID.randomUUID();

        repository.save(new Recipe(
                recipeA,
                groupA,
                "A",
                Instant.parse("2026-02-01T10:00:00Z"),
                List.of()
        ));
        repository.save(new Recipe(
                recipeB,
                groupB,
                "B",
                Instant.parse("2026-02-01T10:00:00Z"),
                List.of()
        ));

        List<Recipe> result = repository.findByGroupIdAndIds(groupA, Set.of(recipeA, recipeB));

        assertThat(result).extracting(Recipe::getId).containsExactly(recipeA);
    }

    @Test
    void repeatedSaveReplacesIngredientsForSameRecipeId() {
        UUID groupId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-02-01T10:00:00Z");

        repository.save(new Recipe(
                recipeId,
                groupId,
                "Soup",
                null,
                null,
                null,
                createdAt,
                List.of(
                        new Ingredient(UUID.randomUUID(), "Tomato", null, null, 1),
                        new Ingredient(UUID.randomUUID(), "Milk", new BigDecimal("2.0"), IngredientUnit.DL, 2)
                )
        ));

        repository.save(new Recipe(
                recipeId,
                groupId,
                "Soup Updated",
                "Notebook",
                "https://example.com/updated-soup",
                RecipeOriginKind.URL_IMPORT,
                "6 servings",
                "Updated note",
                "Updated instructions",
                createdAt,
                Instant.parse("2026-02-03T12:00:00Z"),
                List.of(
                        new Ingredient(UUID.randomUUID(), "Onion", null, null, 1),
                        new Ingredient(UUID.randomUUID(), "Water", new BigDecimal("1.0"), IngredientUnit.L, 2)
                )
        ));

        Optional<Recipe> loaded = repository.findByIdAndGroupId(recipeId, groupId);

        assertThat(loaded).isPresent();
        assertThat(loaded.get().getName()).isEqualTo("Soup Updated");
        assertThat(loaded.get().getSourceName()).isEqualTo("Notebook");
        assertThat(loaded.get().getSourceUrl()).isEqualTo("https://example.com/updated-soup");
        assertThat(loaded.get().getOriginKind()).isEqualTo(RecipeOriginKind.URL_IMPORT);
        assertThat(loaded.get().getServings()).isEqualTo("6 servings");
        assertThat(loaded.get().getShortNote()).isEqualTo("Updated note");
        assertThat(loaded.get().getInstructions()).isEqualTo("Updated instructions");
        assertThat(loaded.get().getUpdatedAt()).isEqualTo(Instant.parse("2026-02-03T12:00:00Z"));
        assertThat(loaded.get().getIngredients())
                .extracting(Ingredient::getName)
                .containsExactly("Onion", "Water");
        assertThat(loaded.get().getIngredients())
                .extracting(Ingredient::getPosition)
                .containsExactly(1, 2);
    }

    @Test
    void deleteRemovesRecipeByGroupScopedIdentity() {
        UUID groupId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        Recipe recipe = new Recipe(
                recipeId,
                groupId,
                "Archived Soup",
                null,
                null,
                RecipeOriginKind.MANUAL,
                null,
                null,
                Instant.parse("2026-02-01T10:00:00Z"),
                Instant.parse("2026-02-03T10:00:00Z"),
                Instant.parse("2026-02-03T10:00:00Z"),
                true,
                List.of()
        );

        repository.save(recipe);
        repository.delete(recipe);

        assertThat(repository.findByIdAndGroupId(recipeId, groupId)).isEmpty();
    }
}
