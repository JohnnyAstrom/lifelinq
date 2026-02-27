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
                listId
        );

        InOrder order = inOrder(shopping);
        order.verify(shopping).addShoppingItem(
                eq(groupId),
                eq(userId),
                eq(listId),
                eq("olive oil"),
                eq(new BigDecimal("2")),
                eq("DL")
        );
        order.verify(shopping).addShoppingItem(
                eq(groupId),
                eq(userId),
                eq(listId),
                eq("tomato"),
                eq(null),
                eq(null)
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

        service.addOrReplaceMeal(groupId, userId, 2026, 5, 1, MealType.DINNER, recipeId, listId);

        verify(shopping, times(2)).addShoppingItem(groupId, userId, listId, "tomato", null, null);
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
                UUID.randomUUID()
        )).isInstanceOf(RecipeNotFoundException.class);

        verify(shopping, never()).addShoppingItem(any(), any(), any(), any(), any(), any());
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
                List.of(
                        new IngredientInput("Milk", null, null, 1),
                        new IngredientInput("Water", null, null, 1)
                )
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positions must be unique");
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
                groupId, userId, 2026, 5, 1, MealType.DINNER, recipeId, null
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
                groupId, userId, 2026, 5, 1, MealType.DINNER, recipeId, listId
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
                groupId, userId, 2026, 5, 1, MealType.DINNER, recipeId, listId
        );

        InOrder order = inOrder(shopping);
        order.verify(shopping).addShoppingItem(groupId, userId, listId, "tomato", null, null);
        order.verify(shopping).addShoppingItem(groupId, userId, listId, "onion", null, null);
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
    }

    private static final class InMemoryRecipeRepository implements RecipeRepository {
        private final Map<UUID, Recipe> recipes = new HashMap<>();

        @Override
        public Recipe save(Recipe recipe) {
            recipes.put(recipe.getId(), recipe);
            return recipe;
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
        public List<Recipe> findByGroupId(UUID groupId) {
            List<Recipe> result = new ArrayList<>();
            for (Recipe recipe : recipes.values()) {
                if (recipe.getGroupId().equals(groupId)) {
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
