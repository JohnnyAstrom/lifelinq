package app.lifelinq.features.meals.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import app.lifelinq.features.group.contract.EnsureGroupMemberUseCase;
import app.lifelinq.features.meals.contract.IngredientInput;
import app.lifelinq.features.meals.contract.MealsShoppingPort;
import app.lifelinq.features.meals.domain.IngredientUnit;
import app.lifelinq.features.meals.domain.MealType;
import app.lifelinq.features.meals.domain.Recipe;
import app.lifelinq.features.meals.domain.RecipeRepository;
import app.lifelinq.features.meals.domain.WeekPlan;
import app.lifelinq.features.meals.domain.WeekPlanRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class MealsApplicationServiceTest {

    @Test
    void addMealPushesIngredientsInPositionOrderWithLockedNormalization() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        UUID listId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        MealsShoppingPort shopping = mock(MealsShoppingPort.class);
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                shopping,
                Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC)
        );

        recipes.save(new Recipe(
                recipeId,
                groupId,
                "Dinner",
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of(
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                                "  Olive   Oil ",
                                new BigDecimal("2"),
                                IngredientUnit.DL,
                                2
                        ),
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                                "  TOMATO  ",
                                null,
                                null,
                                1
                        )
                )
        ));

        service.addOrReplaceMeal(
                groupId,
                userId,
                2026,
                5,
                1,
                MealType.DINNER,
                recipeId,
                listId,
                null
        );

        InOrder order = inOrder(shopping);
        order.verify(shopping).addShoppingItem(
                eq(groupId),
                eq(userId),
                eq(listId),
                eq("olive oil"),
                eq(new BigDecimal("2")),
                eq("DL"),
                eq("meal-plan"),
                eq("Dinner")
        );
        order.verify(shopping).addShoppingItem(
                eq(groupId),
                eq(userId),
                eq(listId),
                eq("tomato"),
                eq(null),
                eq(null),
                eq("meal-plan"),
                eq("Dinner")
        );
    }

    @Test
    void duplicateIngredientNamesArePushedAsSeparateOccurrences() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        UUID listId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        MealsShoppingPort shopping = mock(MealsShoppingPort.class);
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                shopping,
                Clock.systemUTC()
        );

        recipes.save(new Recipe(
                recipeId,
                groupId,
                "Soup",
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of(
                        new app.lifelinq.features.meals.domain.Ingredient(UUID.randomUUID(), "Tomato", null, null, 1),
                        new app.lifelinq.features.meals.domain.Ingredient(UUID.randomUUID(), "tomato", null, null, 2)
                )
        ));

        service.addOrReplaceMeal(groupId, userId, 2026, 5, 1, MealType.DINNER, recipeId, listId, null);

        verify(shopping, times(2)).addShoppingItem(groupId, userId, listId, "tomato", null, null, "meal-plan", "Soup");
    }

    @Test
    void recipeMissingInGroupFailsAndNoShoppingCallsAreMade() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        MealsShoppingPort shopping = mock(MealsShoppingPort.class);
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                shopping,
                Clock.systemUTC()
        );

        assertThatThrownBy(() -> service.addOrReplaceMeal(
                groupId,
                userId,
                2026,
                5,
                1,
                MealType.DINNER,
                recipeId,
                UUID.randomUUID(),
                null
        )).isInstanceOf(RecipeNotFoundException.class);

        verify(shopping, never()).addShoppingItem(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void createRecipeRejectsDuplicatePositions() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        MealsApplicationService service = new MealsApplicationService(
                new InMemoryWeekPlanRepository(),
                new InMemoryRecipeRepository(),
                membership,
                mock(MealsShoppingPort.class),
                Clock.systemUTC()
        );

        assertThatThrownBy(() -> service.createRecipe(
                groupId,
                userId,
                "Recipe",
                null,
                null,
                null,
                null,
                null,
                List.of(
                        new IngredientInput("Milk", null, null, 1),
                        new IngredientInput("Water", null, null, 1)
                )
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positions must be unique");
    }

    @Test
    void createRecipeCarriesRecipeContentFields() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        MealsApplicationService service = new MealsApplicationService(
                new InMemoryWeekPlanRepository(),
                new InMemoryRecipeRepository(),
                membership,
                mock(MealsShoppingPort.class),
                Clock.fixed(Instant.parse("2026-02-01T10:00:00Z"), ZoneOffset.UTC)
        );

        var created = service.createRecipe(
                groupId,
                userId,
                "Recipe",
                "Grandma's notebook",
                "https://example.com/grandma-recipe",
                "URL_IMPORT",
                "Best for weekends",
                "Mix ingredients\nBake for 20 minutes",
                List.of()
        );

        assertThat(created.sourceName()).isEqualTo("Grandma's notebook");
        assertThat(created.sourceUrl()).isEqualTo("https://example.com/grandma-recipe");
        assertThat(created.originKind()).isEqualTo("URL_IMPORT");
        assertThat(created.shortNote()).isEqualTo("Best for weekends");
        assertThat(created.instructions()).isEqualTo("Mix ingredients\nBake for 20 minutes");
        assertThat(created.updatedAt()).isEqualTo(Instant.parse("2026-02-01T10:00:00Z"));
        assertThat(created.archivedAt()).isNull();
    }

    @Test
    void archiveRecipeRetiresItFromActiveListButKeepsItReadable() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        MealsApplicationService service = new MealsApplicationService(
                new InMemoryWeekPlanRepository(),
                recipes,
                membership,
                mock(MealsShoppingPort.class),
                Clock.fixed(Instant.parse("2026-03-18T10:00:00Z"), ZoneOffset.UTC)
        );

        recipes.save(new Recipe(
                recipeId,
                groupId,
                "Archive Me",
                Instant.parse("2026-03-10T09:00:00Z"),
                List.of()
        ));

        var archived = service.archiveRecipe(groupId, userId, recipeId);
        var activeRecipes = service.listRecipes(groupId, userId);
        var loaded = service.getRecipe(groupId, userId, recipeId);

        assertThat(archived.archivedAt()).isEqualTo(Instant.parse("2026-03-18T10:00:00Z"));
        assertThat(archived.updatedAt()).isEqualTo(Instant.parse("2026-03-18T10:00:00Z"));
        assertThat(activeRecipes).isEmpty();
        assertThat(loaded.recipeId()).isEqualTo(recipeId);
        assertThat(loaded.archivedAt()).isEqualTo(Instant.parse("2026-03-18T10:00:00Z"));
    }

    @Test
    void listArchivedRecipesReturnsArchivedRecipesOnly() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        MealsApplicationService service = new MealsApplicationService(
                new InMemoryWeekPlanRepository(),
                recipes,
                membership,
                mock(MealsShoppingPort.class),
                Clock.fixed(Instant.parse("2026-03-18T10:00:00Z"), ZoneOffset.UTC)
        );

        recipes.save(new Recipe(
                UUID.randomUUID(),
                groupId,
                "Active Recipe",
                Instant.parse("2026-03-10T09:00:00Z"),
                List.of()
        ));
        recipes.save(new Recipe(
                UUID.randomUUID(),
                groupId,
                "Archived Recipe",
                null,
                null,
                null,
                null,
                null,
                Instant.parse("2026-03-10T09:00:00Z"),
                Instant.parse("2026-03-18T10:00:00Z"),
                Instant.parse("2026-03-18T10:00:00Z"),
                List.of()
        ));

        var archivedRecipes = service.listArchivedRecipes(groupId, userId);

        assertThat(archivedRecipes).extracting(view -> view.name()).containsExactly("Archived Recipe");
    }

    @Test
    void restoreRecipeReturnsRecipeToActiveList() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        MealsApplicationService service = new MealsApplicationService(
                new InMemoryWeekPlanRepository(),
                recipes,
                membership,
                mock(MealsShoppingPort.class),
                Clock.fixed(Instant.parse("2026-03-20T10:00:00Z"), ZoneOffset.UTC)
        );

        recipes.save(new Recipe(
                recipeId,
                groupId,
                "Archived Recipe",
                null,
                null,
                null,
                null,
                null,
                Instant.parse("2026-03-10T09:00:00Z"),
                Instant.parse("2026-03-18T10:00:00Z"),
                Instant.parse("2026-03-18T10:00:00Z"),
                List.of()
        ));

        var restored = service.restoreRecipe(groupId, userId, recipeId);

        assertThat(restored.archivedAt()).isNull();
        assertThat(restored.updatedAt()).isEqualTo(Instant.parse("2026-03-20T10:00:00Z"));
        assertThat(service.listRecipes(groupId, userId)).extracting(view -> view.name()).containsExactly("Archived Recipe");
        assertThat(service.listArchivedRecipes(groupId, userId)).isEmpty();
    }

    @Test
    void getArchivedRecipeIncludesBlockedDeleteReasonWhenStillPlannedForCurrentWeek() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                mock(MealsShoppingPort.class),
                Clock.fixed(Instant.parse("2026-03-20T10:00:00Z"), ZoneOffset.UTC)
        );

        recipes.save(new Recipe(
                recipeId,
                groupId,
                "Archived Recipe",
                null,
                null,
                null,
                null,
                null,
                Instant.parse("2026-03-10T09:00:00Z"),
                Instant.parse("2026-03-18T10:00:00Z"),
                Instant.parse("2026-03-18T10:00:00Z"),
                List.of()
        ));
        service.addOrReplaceMeal(groupId, userId, 2026, 12, 1, MealType.DINNER, recipeId, null, null);

        var recipe = service.getRecipe(groupId, userId, recipeId);

        assertThat(recipe.deleteEligible()).isFalse();
        assertThat(recipe.deleteBlockedReason()).isEqualTo("This recipe is still used in planned meals.");
    }

    @Test
    void deleteRecipeRemovesUnusedArchivedRecipe() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        MealsApplicationService service = new MealsApplicationService(
                new InMemoryWeekPlanRepository(),
                recipes,
                membership,
                mock(MealsShoppingPort.class),
                Clock.systemUTC()
        );

        recipes.save(new Recipe(
                recipeId,
                groupId,
                "Archived Recipe",
                null,
                null,
                null,
                null,
                null,
                Instant.parse("2026-03-10T09:00:00Z"),
                Instant.parse("2026-03-18T10:00:00Z"),
                Instant.parse("2026-03-18T10:00:00Z"),
                List.of()
        ));

        service.deleteRecipe(groupId, userId, recipeId);

        assertThat(recipes.findByIdAndGroupId(recipeId, groupId)).isEmpty();
    }

    @Test
    void deleteRecipeIsBlockedWhenRecipeIsNotArchived() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        MealsApplicationService service = new MealsApplicationService(
                new InMemoryWeekPlanRepository(),
                recipes,
                membership,
                mock(MealsShoppingPort.class),
                Clock.systemUTC()
        );

        recipes.save(new Recipe(
                recipeId,
                groupId,
                "Active Recipe",
                Instant.parse("2026-03-10T09:00:00Z"),
                List.of()
        ));

        assertThatThrownBy(() -> service.deleteRecipe(groupId, userId, recipeId))
                .isInstanceOf(RecipeDeleteBlockedException.class)
                .hasMessage("Recipe must be archived before you can delete it.");
    }

    @Test
    void deleteRecipeIsBlockedWhenArchivedRecipeIsStillPlannedForCurrentOrFutureWeek() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                mock(MealsShoppingPort.class),
                Clock.fixed(Instant.parse("2026-03-20T10:00:00Z"), ZoneOffset.UTC)
        );

        recipes.save(new Recipe(
                recipeId,
                groupId,
                "Archived Recipe",
                null,
                null,
                null,
                null,
                null,
                Instant.parse("2026-03-10T09:00:00Z"),
                Instant.parse("2026-03-18T10:00:00Z"),
                Instant.parse("2026-03-18T10:00:00Z"),
                List.of()
        ));
        service.addOrReplaceMeal(groupId, userId, 2026, 12, 1, MealType.DINNER, recipeId, null, null);

        assertThatThrownBy(() -> service.deleteRecipe(groupId, userId, recipeId))
                .isInstanceOf(RecipeDeleteBlockedException.class)
                .hasMessage("This recipe is still used in planned meals.");
    }

    @Test
    void deleteRecipeAllowsPastOnlyHistoricalUsage() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                mock(MealsShoppingPort.class),
                Clock.fixed(Instant.parse("2026-03-20T10:00:00Z"), ZoneOffset.UTC)
        );

        recipes.save(new Recipe(
                recipeId,
                groupId,
                "Archived Recipe",
                null,
                null,
                null,
                null,
                null,
                Instant.parse("2026-03-10T09:00:00Z"),
                Instant.parse("2026-03-18T10:00:00Z"),
                Instant.parse("2026-03-18T10:00:00Z"),
                List.of()
        ));
        service.addOrReplaceMeal(groupId, userId, 2026, 11, 1, MealType.DINNER, recipeId, null, null);

        service.deleteRecipe(groupId, userId, recipeId);

        assertThat(recipes.findByIdAndGroupId(recipeId, groupId)).isEmpty();
    }

    @Test
    void getWeekPlanUsesRuntimeRecipeNameLookup() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                mock(MealsShoppingPort.class),
                Clock.systemUTC()
        );

        recipes.save(new Recipe(
                recipeId,
                groupId,
                "Old Name",
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(), "Tomato", null, null, 1))
        ));

        service.addOrReplaceMeal(
                groupId, userId, 2026, 5, 1, MealType.DINNER, recipeId, null, null
        );

        recipes.save(new Recipe(
                recipeId,
                groupId,
                "New Name",
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(), "Tomato", null, null, 1))
        ));

        var weekPlan = service.getWeekPlan(groupId, userId, 2026, 5);
        assertThat(weekPlan.meals()).hasSize(1);
        assertThat(weekPlan.meals().get(0).recipeTitle()).isEqualTo("New Name");
    }

    @Test
    void getWeekPlanStillResolvesArchivedRecipeNames() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                mock(MealsShoppingPort.class),
                Clock.fixed(Instant.parse("2026-03-18T10:00:00Z"), ZoneOffset.UTC)
        );

        recipes.save(new Recipe(
                recipeId,
                groupId,
                "Archived Recipe",
                Instant.parse("2026-03-10T09:00:00Z"),
                List.of()
        ));

        service.addOrReplaceMeal(groupId, userId, 2026, 12, 1, MealType.DINNER, recipeId, null, null);
        service.archiveRecipe(groupId, userId, recipeId);

        var weekPlan = service.getWeekPlan(groupId, userId, 2026, 12);

        assertThat(weekPlan.meals()).hasSize(1);
        assertThat(weekPlan.meals().get(0).recipeTitle()).isEqualTo("Archived Recipe");
    }

    @Test
    void getWeekPlanFallsBackToStoredRecipeTitleAfterRecipeDelete() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                mock(MealsShoppingPort.class),
                Clock.fixed(Instant.parse("2026-03-20T10:00:00Z"), ZoneOffset.UTC)
        );

        recipes.save(new Recipe(
                recipeId,
                groupId,
                "Historical Recipe",
                Instant.parse("2026-03-01T09:00:00Z"),
                List.of()
        ));

        service.addOrReplaceMeal(groupId, userId, 2026, 11, 1, MealType.DINNER, recipeId, null, null);
        service.archiveRecipe(groupId, userId, recipeId);
        service.deleteRecipe(groupId, userId, recipeId);

        var weekPlan = service.getWeekPlan(groupId, userId, 2026, 11);

        assertThat(weekPlan.meals()).hasSize(1);
        assertThat(weekPlan.meals().get(0).recipeTitle()).isEqualTo("Historical Recipe");
    }

    @Test
    void addMealPushUsesCurrentRecipeIngredientsForSameRecipeId() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        UUID listId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        MealsShoppingPort shopping = mock(MealsShoppingPort.class);
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                shopping,
                Clock.systemUTC()
        );

        recipes.save(new Recipe(
                recipeId,
                groupId,
                "Recipe",
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(), "Tomato", null, null, 1))
        ));

        service.addOrReplaceMeal(
                groupId, userId, 2026, 5, 1, MealType.DINNER, recipeId, listId, null
        );

        recipes.save(new Recipe(
                recipeId,
                groupId,
                "Recipe",
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(), "Onion", null, null, 1))
        ));

        service.addOrReplaceMeal(
                groupId, userId, 2026, 5, 1, MealType.DINNER, recipeId, listId, null
        );

        InOrder order = inOrder(shopping);
        order.verify(shopping).addShoppingItem(groupId, userId, listId, "tomato", null, null, "meal-plan", "Recipe");
        order.verify(shopping).addShoppingItem(groupId, userId, listId, "onion", null, null, "meal-plan", "Recipe");
    }

    @Test
    void addMealPushesOnlySelectedIngredientPositions() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        UUID listId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        MealsShoppingPort shopping = mock(MealsShoppingPort.class);
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                shopping,
                Clock.systemUTC()
        );

        recipes.save(new Recipe(
                recipeId,
                groupId,
                "Soup",
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of(
                        new app.lifelinq.features.meals.domain.Ingredient(UUID.randomUUID(), "Tomato", null, null, 1),
                        new app.lifelinq.features.meals.domain.Ingredient(UUID.randomUUID(), "Onion", null, null, 2),
                        new app.lifelinq.features.meals.domain.Ingredient(UUID.randomUUID(), "Milk", null, null, 3)
                )
        ));

        service.addOrReplaceMeal(
                groupId, userId, 2026, 5, 1, MealType.DINNER, recipeId, listId, List.of(1, 3)
        );

        InOrder order = inOrder(shopping);
        order.verify(shopping).addShoppingItem(groupId, userId, listId, "milk", null, null, "meal-plan", "Soup");
        order.verify(shopping).addShoppingItem(groupId, userId, listId, "tomato", null, null, "meal-plan", "Soup");
        verify(shopping, never()).addShoppingItem(groupId, userId, listId, "onion", null, null, "meal-plan", "Soup");
    }

    private static final class InMemoryWeekPlanRepository implements WeekPlanRepository {
        private final Map<UUID, WeekPlan> byId = new HashMap<>();

        @Override
        public WeekPlan save(WeekPlan weekPlan) {
            byId.put(weekPlan.getId(), weekPlan);
            return weekPlan;
        }

        @Override
        public Optional<WeekPlan> findByGroupAndWeek(UUID groupId, int year, int isoWeek) {
            return byId.values().stream()
                    .filter(plan -> plan.getGroupId().equals(groupId))
                    .filter(plan -> plan.getYear() == year)
                    .filter(plan -> plan.getIsoWeek() == isoWeek)
                    .findFirst();
        }

        @Override
        public Optional<WeekPlan> findById(UUID id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public boolean existsCurrentOrFutureMealReferencingRecipe(UUID groupId, UUID recipeId, int year, int isoWeek) {
            return byId.values().stream()
                    .filter(plan -> plan.getGroupId().equals(groupId))
                    .filter(plan -> plan.getYear() > year || (plan.getYear() == year && plan.getIsoWeek() >= isoWeek))
                    .flatMap(plan -> plan.getMeals().stream())
                    .anyMatch(meal -> meal.getRecipeId().equals(recipeId));
        }
    }

    private static final class InMemoryRecipeRepository implements RecipeRepository {
        private final Map<UUID, Recipe> recipes = new HashMap<>();

        @Override
        public Recipe save(Recipe recipe) {
            recipes.put(recipe.getId(), recipe);
            return recipe;
        }

        @Override
        public void delete(Recipe recipe) {
            recipes.remove(recipe.getId());
        }

        @Override
        public Optional<Recipe> findByIdAndGroupId(UUID recipeId, UUID groupId) {
            Recipe recipe = recipes.get(recipeId);
            if (recipe == null || !recipe.getGroupId().equals(groupId)) {
                return Optional.empty();
            }
            return Optional.of(recipe);
        }

        @Override
        public List<Recipe> findActiveByGroupId(UUID groupId) {
            List<Recipe> result = new ArrayList<>();
            for (Recipe recipe : recipes.values()) {
                if (recipe.getGroupId().equals(groupId) && !recipe.isArchived()) {
                    result.add(recipe);
                }
            }
            return result;
        }

        @Override
        public List<Recipe> findArchivedByGroupId(UUID groupId) {
            List<Recipe> result = new ArrayList<>();
            for (Recipe recipe : recipes.values()) {
                if (recipe.getGroupId().equals(groupId) && recipe.isArchived()) {
                    result.add(recipe);
                }
            }
            return result;
        }

        @Override
        public List<Recipe> findByGroupIdAndIds(UUID groupId, Set<UUID> recipeIds) {
            List<Recipe> result = new ArrayList<>();
            for (UUID recipeId : recipeIds) {
                Recipe recipe = recipes.get(recipeId);
                if (recipe != null && recipe.getGroupId().equals(groupId)) {
                    result.add(recipe);
                }
            }
            return result;
        }
    }
}
