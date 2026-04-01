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
import app.lifelinq.features.meals.contract.ParsedRecipeImportData;
import app.lifelinq.features.meals.contract.RecipeImportPort;
import app.lifelinq.features.meals.domain.HouseholdPreferenceSignal;
import app.lifelinq.features.meals.domain.HouseholdPreferenceSignalRepository;
import app.lifelinq.features.meals.domain.HouseholdPreferenceSignalTargetKind;
import app.lifelinq.features.meals.domain.HouseholdPreferenceSignalType;
import app.lifelinq.features.meals.domain.IngredientUnit;
import app.lifelinq.features.meals.domain.MealMemoryRepository;
import app.lifelinq.features.meals.domain.MealOccurrence;
import app.lifelinq.features.meals.domain.MealType;
import app.lifelinq.features.meals.domain.RecentPlannedMeal;
import app.lifelinq.features.meals.domain.Recipe;
import app.lifelinq.features.meals.domain.RecipeOriginKind;
import app.lifelinq.features.meals.domain.RecipeRepository;
import app.lifelinq.features.meals.domain.WeekPlan;
import app.lifelinq.features.meals.domain.WeekPlanRepository;
import app.lifelinq.features.meals.infrastructure.InMemoryRecipeDraftRepository;
import app.lifelinq.features.shopping.application.ShoppingApplicationService;
import app.lifelinq.features.shopping.domain.ShoppingListType;
import app.lifelinq.features.shopping.domain.ShoppingUnit;
import app.lifelinq.features.shopping.infrastructure.InMemoryShoppingCategoryPreferenceRepository;
import app.lifelinq.features.shopping.infrastructure.InMemoryShoppingListRepository;
import app.lifelinq.features.shopping.infrastructure.MealsShoppingPortAdapter;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
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
    void manualDraftCanBeRefinedAndAcceptedIntoLibrary() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        InMemoryRecipeDraftRepository drafts = new InMemoryRecipeDraftRepository();
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                drafts,
                null,
                membership,
                mock(MealsShoppingPort.class),
                Clock.fixed(Instant.parse("2026-03-24T10:00:00Z"), ZoneOffset.UTC)
        );

        var created = service.createManualRecipeDraft(groupId, userId);
        assertThat(created.state()).isEqualTo("draft_open");

        var updated = service.updateRecipeDraft(
                groupId,
                userId,
                created.draftId(),
                "Weeknight Pasta",
                "Family notebook",
                null,
                "4 servings",
                "Easy fallback",
                "Boil pasta\nMix sauce",
                false,
                List.of(new IngredientInput("Pasta", null, null, null, 1))
        );

        assertThat(updated.state()).isEqualTo("draft_ready");
        assertThat(updated.source().sourceName()).isEqualTo("Family notebook");

        var assessment = service.getRecipeDraftDuplicateAssessment(groupId, userId, created.draftId());
        assertThat(assessment.attentionRequired()).isFalse();

        var saved = service.acceptRecipeDraft(groupId, userId, created.draftId(), false);
        assertThat(saved.name()).isEqualTo("Weeknight Pasta");
        assertThat(saved.lifecycle().state()).isEqualTo("active");
        assertThat(saved.provenance().originKind()).isEqualTo("manual");
        assertThat(recipes.findActiveByGroupId(groupId)).hasSize(1);
        assertThatThrownBy(() -> service.getRecipeDraft(groupId, userId, created.draftId()))
                .isInstanceOf(RecipeDraftNotFoundException.class);
    }

    @Test
    void importedDraftStartsInNeedsReviewAndRequiresDuplicateAttentionForSameSourceUrl() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        InMemoryRecipeDraftRepository drafts = new InMemoryRecipeDraftRepository();
        RecipeImportPort importPort = url -> new ParsedRecipeImportData(
                "Apple Pie",
                "Example Kitchen",
                url,
                "8 slices",
                "Weekend dessert",
                "Mix and bake",
                List.of("2 apples", "1 dl sugar")
        );
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                drafts,
                importPort,
                membership,
                mock(MealsShoppingPort.class),
                Clock.fixed(Instant.parse("2026-03-24T10:00:00Z"), ZoneOffset.UTC)
        );

        recipes.save(new Recipe(
                UUID.randomUUID(),
                groupId,
                "Apple Pie",
                "Example Kitchen",
                "https://example.com/pie",
                RecipeOriginKind.URL_IMPORT,
                "8 slices",
                null,
                "Weekend dessert",
                "Mix and bake",
                Instant.parse("2026-03-10T10:00:00Z"),
                Instant.parse("2026-03-10T10:00:00Z"),
                null,
                true,
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(),
                        "Apples",
                        null,
                        null,
                        1
                ))
        ));

        var draft = service.createRecipeDraftFromUrl(groupId, userId, "https://example.com/pie");
        assertThat(draft.state()).isEqualTo("draft_needs_review");
        assertThat(draft.provenance().originKind()).isEqualTo("url_import");

        var assessment = service.getRecipeDraftDuplicateAssessment(groupId, userId, draft.draftId());
        assertThat(assessment.attentionRequired()).isTrue();
        assertThat(assessment.matchType()).isEqualTo("exact_source_url");
        assertThat(assessment.matchingRecipe()).isNotNull();
        assertThatThrownBy(() -> service.acceptRecipeDraft(groupId, userId, draft.draftId(), false))
                .isInstanceOf(RecipeDuplicateAttentionRequiredException.class);
    }

    @Test
    void pastedTextDraftStartsInNeedsReviewAndCanFlowThroughDraftReviewAndAccept() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        InMemoryRecipeDraftRepository drafts = new InMemoryRecipeDraftRepository();
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                drafts,
                null,
                membership,
                mock(MealsShoppingPort.class),
                Clock.fixed(Instant.parse("2026-03-24T10:00:00Z"), ZoneOffset.UTC)
        );

        var created = service.createRecipeDraftFromText(
                groupId,
                userId,
                """
                Weeknight Pasta

                Ingredients
                1 pack pasta
                2 dl cream

                Instructions
                Boil pasta.
                Mix sauce.
                """
        );

        assertThat(created.state()).isEqualTo("draft_needs_review");
        assertThat(created.name()).isEqualTo("Weeknight Pasta");
        assertThat(created.provenance().originKind()).isEqualTo("pasted_text");
        assertThat(created.source().sourceUrl()).isNull();
        assertThat(created.ingredients()).hasSize(2);
        assertThat(created.instructions()).contains("Boil pasta.");

        var updated = service.updateRecipeDraft(
                groupId,
                userId,
                created.draftId(),
                created.name(),
                null,
                null,
                null,
                null,
                created.instructions(),
                true,
                List.of(
                        new IngredientInput("pasta", null, new java.math.BigDecimal("1"), IngredientUnit.PACK, 1),
                        new IngredientInput("cream", null, new java.math.BigDecimal("2"), IngredientUnit.DL, 2),
                        new IngredientInput("salt", null, null, null, 3)
                )
        );

        assertThat(updated.state()).isEqualTo("draft_ready");
        assertThat(service.getRecipeDraftDuplicateAssessment(groupId, userId, created.draftId()).attentionRequired())
                .isFalse();

        var saved = service.acceptRecipeDraft(groupId, userId, created.draftId(), false);
        assertThat(saved.provenance().originKind()).isEqualTo("pasted_text");
        assertThat(saved.lifecycle().state()).isEqualTo("active");
        assertThat(saved.ingredients()).hasSize(3);
    }

    @Test
    void pastedTextDraftCanBeCreatedWithoutExtractedIngredientsWhenTextIsStillReviewable() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        InMemoryRecipeDraftRepository drafts = new InMemoryRecipeDraftRepository();
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                drafts,
                null,
                membership,
                mock(MealsShoppingPort.class),
                Clock.fixed(Instant.parse("2026-03-24T10:00:00Z"), ZoneOffset.UTC)
        );

        var created = service.createRecipeDraftFromText(
                groupId,
                userId,
                """
                Quick Tomato Sauce

                Simmer everything gently until the sauce thickens.
                Taste and adjust before serving with pasta.
                """
        );

        assertThat(created.state()).isEqualTo("draft_needs_review");
        assertThat(created.name()).isEqualTo("Quick Tomato Sauce");
        assertThat(created.provenance().originKind()).isEqualTo("pasted_text");
        assertThat(created.ingredients()).isEmpty();
        assertThat(created.instructions()).contains("Simmer everything gently");
    }

    @Test
    void pastedTextDraftStillRejectsTextThatIsTooThinToReviewMeaningfully() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        InMemoryRecipeDraftRepository drafts = new InMemoryRecipeDraftRepository();
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                drafts,
                null,
                membership,
                mock(MealsShoppingPort.class),
                Clock.fixed(Instant.parse("2026-03-24T10:00:00Z"), ZoneOffset.UTC)
        );

        assertThatThrownBy(() -> service.createRecipeDraftFromText(
                groupId,
                userId,
                """
                Pasta
                Nice.
                """
        ))
                .isInstanceOf(RecipeImportFailedException.class)
                .hasMessageContaining("Paste a little more of the recipe");
    }

    @Test
    void program2FoundationExposesRecentIdentityAndRecipeUsageMemoryFromWeekPlans() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        InMemoryHouseholdPreferenceSignalRepository preferences = new InMemoryHouseholdPreferenceSignalRepository();
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                null,
                weekPlans,
                preferences,
                null,
                membership,
                mock(MealsShoppingPort.class),
                Clock.fixed(Instant.parse("2026-03-24T10:00:00Z"), ZoneOffset.UTC)
        );

        recipes.save(new Recipe(
                recipeId,
                groupId,
                "Taco Soup",
                null,
                null,
                RecipeOriginKind.MANUAL,
                "4 servings",
                Instant.parse("2026-03-24T09:00:00Z"),
                "Weeknight soup",
                "Cook gently",
                Instant.parse("2026-03-01T09:00:00Z"),
                Instant.parse("2026-03-24T09:00:00Z"),
                null,
                true,
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(), "Beans", null, null, 1))
        ));
        service.addOrReplaceMeal(groupId, userId, 2026, 10, 2, MealType.DINNER, recipeId, null, null);
        service.addOrReplaceMeal(groupId, userId, 2026, 12, 3, MealType.DINNER, recipeId, null, null);
        service.addOrReplaceMeal(groupId, userId, 2026, 11, 1, MealType.DINNER, "Tacos", null, null, null);
        service.addOrReplaceMeal(groupId, userId, 2026, 12, 1, MealType.DINNER, "Tacos", null, null, null);

        var occurrences = service.listRecentMealOccurrences(groupId, userId, 10);
        var identitySummaries = service.listMealIdentitySummaries(groupId, userId, 10);
        var recipeUsage = service.getRecipeUsageSummary(groupId, userId, recipeId);

        assertThat(occurrences).extracting(occurrence -> occurrence.mealIdentityKind())
                .contains("recipe", "title_only");
        assertThat(identitySummaries).anySatisfy(summary -> {
            assertThat(summary.title()).isEqualTo("Tacos");
            assertThat(summary.familiar()).isTrue();
        });
        assertThat(identitySummaries).anySatisfy(summary -> {
            assertThat(summary.recipeId()).isEqualTo(recipeId);
            assertThat(summary.makeSoon()).isTrue();
        });
        assertThat(recipeUsage.recipeId()).isEqualTo(recipeId);
        assertThat(recipeUsage.totalUses()).isEqualTo(2);
        assertThat(recipeUsage.familiar()).isTrue();
        assertThat(recipeUsage.makeSoon()).isTrue();
    }

    @Test
    void program2FoundationStoresHouseholdPreferenceSignalsSeparatelyFromHistory() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        InMemoryHouseholdPreferenceSignalRepository preferences = new InMemoryHouseholdPreferenceSignalRepository();
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                null,
                weekPlans,
                preferences,
                null,
                membership,
                mock(MealsShoppingPort.class),
                Clock.fixed(Instant.parse("2026-03-24T10:00:00Z"), ZoneOffset.UTC)
        );

        recipes.save(new Recipe(
                recipeId,
                groupId,
                "Friday Pasta",
                Instant.parse("2026-03-01T09:00:00Z"),
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(), "Pasta", null, null, 1))
        ));

        var recipeSignal = service.writeHouseholdPreferenceSignal(
                groupId,
                userId,
                "recipe",
                "prefer",
                recipeId,
                null
        );
        var mealSignal = service.writeHouseholdPreferenceSignal(
                groupId,
                userId,
                "meal_identity",
                "fallback",
                null,
                "title:tacos"
        );

        assertThat(recipeSignal.targetKind()).isEqualTo("recipe");
        assertThat(recipeSignal.signalType()).isEqualTo("prefer");
        assertThat(mealSignal.mealIdentityKey()).isEqualTo("title:tacos");

        var preferencesView = service.listHouseholdPreferenceSummaries(groupId, userId);
        assertThat(preferencesView).hasSize(2);

        service.clearHouseholdPreferenceSignal(groupId, userId, "recipe", "prefer", recipeId, null);

        assertThat(service.listHouseholdPreferenceSummaries(groupId, userId)).hasSize(1);
        assertThat(service.listHouseholdPreferenceSummaries(groupId, userId).get(0).signalType()).isEqualTo("fallback");
    }

    @Test
    void program2FoundationBuildsPlanningChoiceSupportFromHistoryPreferencesAndMakeSoon() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        InMemoryHouseholdPreferenceSignalRepository preferences = new InMemoryHouseholdPreferenceSignalRepository();
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                null,
                weekPlans,
                preferences,
                null,
                membership,
                mock(MealsShoppingPort.class),
                Clock.fixed(Instant.parse("2026-03-24T10:00:00Z"), ZoneOffset.UTC)
        );

        recipes.save(new Recipe(
                recipeId,
                groupId,
                "Pasta Bake",
                null,
                null,
                RecipeOriginKind.MANUAL,
                null,
                Instant.parse("2026-03-20T09:00:00Z"),
                null,
                "Bake",
                Instant.parse("2026-03-01T09:00:00Z"),
                Instant.parse("2026-03-20T09:00:00Z"),
                null,
                true,
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(), "Pasta", null, null, 1))
        ));
        service.addOrReplaceMeal(groupId, userId, 2026, 10, 1, MealType.DINNER, recipeId, null, null);
        service.addOrReplaceMeal(groupId, userId, 2026, 11, 3, MealType.DINNER, recipeId, null, null);
        service.addOrReplaceMeal(groupId, userId, 2026, 12, 1, MealType.DINNER, "Tacos", null, null, null);
        service.addOrReplaceMeal(groupId, userId, 2026, 12, 2, MealType.DINNER, "Tacos", null, null, null);
        service.writeHouseholdPreferenceSignal(groupId, userId, "meal_identity", "fallback", null, "title:tacos");

        var support = service.getSlotPlanningChoiceSupport(groupId, userId, 2026, 13, 2, MealType.DINNER);

        assertThat(support.scenario()).isEqualTo("slot");
        assertThat(support.recentCandidates()).isNotEmpty();
        assertThat(support.familiarCandidates()).anySatisfy(candidate -> {
            assertThat(candidate.title()).isEqualTo("Tacos");
            assertThat(candidate.fallback()).isTrue();
        });
        assertThat(support.makeSoonCandidates()).anySatisfy(candidate -> {
            assertThat(candidate.recipeId()).isEqualTo(recipeId);
            assertThat(candidate.makeSoon()).isTrue();
        });
    }

    @Test
    void listRecentlyUsedRecipeLibraryItemsReturnsActiveSavedRecipesInRecentOrder() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID firstRecipeId = UUID.randomUUID();
        UUID secondRecipeId = UUID.randomUUID();
        UUID archivedRecipeId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                mock(MealsShoppingPort.class),
                Clock.fixed(Instant.parse("2026-03-24T10:00:00Z"), ZoneOffset.UTC)
        );

        recipes.save(new Recipe(
                firstRecipeId,
                groupId,
                "Recent Pasta",
                Instant.parse("2026-03-01T09:00:00Z"),
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(), "Pasta", null, null, 1))
        ));
        recipes.save(new Recipe(
                secondRecipeId,
                groupId,
                "Soup",
                Instant.parse("2026-03-01T09:00:00Z"),
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(), "Onion", null, null, 1))
        ));
        recipes.save(new Recipe(
                archivedRecipeId,
                groupId,
                "Archived Pie",
                null,
                null,
                null,
                null,
                null,
                Instant.parse("2026-03-01T09:00:00Z"),
                Instant.parse("2026-03-10T10:00:00Z"),
                Instant.parse("2026-03-10T10:00:00Z"),
                true,
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(), "Apple", null, null, 1))
        ));

        service.addOrReplaceMeal(groupId, userId, 2026, 12, 1, MealType.DINNER, firstRecipeId, null, null);
        service.addOrReplaceMeal(groupId, userId, 2026, 12, 2, MealType.DINNER, archivedRecipeId, null, null);
        service.addOrReplaceMeal(groupId, userId, 2026, 12, 3, MealType.DINNER, secondRecipeId, null, null);

        var recentItems = service.listRecentlyUsedRecipeLibraryItems(groupId, userId);

        assertThat(recentItems).extracting(item -> item.recipeId())
                .containsExactly(secondRecipeId, firstRecipeId);
        assertThat(recentItems).allSatisfy(item -> assertThat(item.lifecycle().state()).isEqualTo("active"));
    }

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
    void addMealReducesCommonCookingNoiseBeforeShoppingPush() {
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
                "Dinner",
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of(
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.randomUUID(),
                                "1 tbsp olive oil plus a little extra",
                                null,
                                null,
                                1
                        ),
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.randomUUID(),
                                "olivolja, till stekning",
                                null,
                                null,
                                2
                        ),
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.randomUUID(),
                                "msk färsk rosmarin, hackad",
                                null,
                                null,
                                3
                        ),
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.randomUUID(),
                                "2 slices prosciutto",
                                null,
                                null,
                                4
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

        verify(shopping).addShoppingItem(groupId, userId, listId, "olive oil", null, null, "meal-plan", "Dinner");
        verify(shopping).addShoppingItem(groupId, userId, listId, "olivolja", null, null, "meal-plan", "Dinner");
        verify(shopping).addShoppingItem(groupId, userId, listId, "färsk rosmarin", null, null, "meal-plan", "Dinner");
        verify(shopping).addShoppingItem(groupId, userId, listId, "prosciutto", null, null, "meal-plan", "Dinner");
    }

    @Test
    void addMealKeepsStructuredQuantityAndUnitWhenOnlyShoppingNoiseIsRemovedFromName() {
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
                "Dinner",
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of(
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.randomUUID(),
                                "Olive oil for frying",
                                new BigDecimal("2"),
                                IngredientUnit.DL,
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

        verify(shopping).addShoppingItem(
                groupId,
                userId,
                listId,
                "olive oil",
                new BigDecimal("2"),
                "DL",
                "meal-plan",
                "Dinner"
        );
    }

    @Test
    void addMealDropsMisleadingPcsWhenRecipeMeasureTokensRemainOnlyAsFallbackNoise() {
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
                "Dinner",
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of(
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.randomUUID(),
                                "½ tbsp english mustard",
                                new BigDecimal("1"),
                                IngredientUnit.PCS,
                                1
                        ),
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.randomUUID(),
                                "1 garlic clove crushed",
                                new BigDecimal("1"),
                                IngredientUnit.PCS,
                                2
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

        verify(shopping).addShoppingItem(groupId, userId, listId, "english mustard", null, null, "meal-plan", "Dinner");
        verify(shopping).addShoppingItem(groupId, userId, listId, "garlic", null, null, "meal-plan", "Dinner");
    }

    @Test
    void addMealStripsCommonLeadingModifiersForShoppingFacingNames() {
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
                "Dinner",
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of(
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.randomUUID(),
                                "finely chopped thyme leaves",
                                null,
                                null,
                                1
                        ),
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.randomUUID(),
                                "finely chopped parsley",
                                null,
                                null,
                                2
                        ),
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.randomUUID(),
                                "large shallot",
                                new BigDecimal("1"),
                                IngredientUnit.PCS,
                                3
                        ),
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.randomUUID(),
                                "chestnut mushroom very finely chopped",
                                null,
                                null,
                                4
                        ),
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.randomUUID(),
                                "mashed root veg",
                                null,
                                null,
                                5
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

        verify(shopping).addShoppingItem(groupId, userId, listId, "thyme", null, null, "meal-plan", "Dinner");
        verify(shopping).addShoppingItem(groupId, userId, listId, "parsley", null, null, "meal-plan", "Dinner");
        verify(shopping).addShoppingItem(groupId, userId, listId, "shallot", new BigDecimal("1"), "PCS", "meal-plan", "Dinner");
        verify(shopping).addShoppingItem(groupId, userId, listId, "chestnut mushroom", null, null, "meal-plan", "Dinner");
        verify(shopping).addShoppingItem(groupId, userId, listId, "root veg", null, null, "meal-plan", "Dinner");
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
    void addMealAllowsLightweightMealTitleWithoutRecipe() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        MealsShoppingPort shopping = mock(MealsShoppingPort.class);
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                new InMemoryRecipeRepository(),
                membership,
                shopping,
                Clock.systemUTC()
        );

        var output = service.addOrReplaceMeal(
                groupId,
                userId,
                2026,
                5,
                1,
                MealType.DINNER,
                "Tacos",
                null,
                null,
                null
        );

        assertThat(output.meal().mealTitle()).isEqualTo("Tacos");
        assertThat(output.meal().recipeId()).isNull();
        assertThat(output.meal().recipeTitle()).isNull();

        var weekPlan = service.getWeekPlan(groupId, userId, 2026, 5);
        assertThat(weekPlan.meals()).hasSize(1);
        assertThat(weekPlan.meals().get(0).mealTitle()).isEqualTo("Tacos");
        assertThat(weekPlan.meals().get(0).recipeId()).isNull();

        verify(shopping, never()).addShoppingItem(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void addMealStoresShoppingHandledSnapshotWhenIngredientsAreSentToShopping() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        UUID listId = UUID.randomUUID();
        Instant now = Instant.parse("2026-03-20T10:00:00Z");
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                mock(MealsShoppingPort.class),
                Clock.fixed(now, ZoneOffset.UTC)
        );

        recipes.save(new Recipe(
                recipeId,
                groupId,
                "Soup",
                Instant.parse("2026-03-01T09:00:00Z"),
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(), "Tomato", null, null, 1))
        ));

        var output = service.addOrReplaceMeal(groupId, userId, 2026, 12, 1, MealType.DINNER, recipeId, listId, List.of(1));

        assertThat(output.meal().shoppingHandledAt()).isEqualTo(now);
        assertThat(output.meal().shoppingListId()).isEqualTo(listId);

        var weekPlan = service.getWeekPlan(groupId, userId, 2026, 12);
        assertThat(weekPlan.meals()).hasSize(1);
        assertThat(weekPlan.meals().get(0).shoppingHandledAt()).isEqualTo(now);
        assertThat(weekPlan.meals().get(0).shoppingListId()).isEqualTo(listId);
    }

    @Test
    void addMealPreservesShoppingHandledSnapshotWhenMealContentIsUnchanged() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        UUID listId = UUID.randomUUID();
        Instant handledAt = Instant.parse("2026-03-20T10:00:00Z");
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                mock(MealsShoppingPort.class),
                Clock.fixed(handledAt, ZoneOffset.UTC)
        );

        recipes.save(new Recipe(
                recipeId,
                groupId,
                "Soup",
                Instant.parse("2026-03-01T09:00:00Z"),
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(), "Tomato", null, null, 1))
        ));

        service.addOrReplaceMeal(groupId, userId, 2026, 12, 1, MealType.DINNER, recipeId, listId, List.of(1));

        MealsApplicationService preservingService = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                mock(MealsShoppingPort.class),
                Clock.fixed(handledAt.plusSeconds(3600), ZoneOffset.UTC)
        );
        var output = preservingService.addOrReplaceMeal(
                groupId,
                userId,
                2026,
                12,
                1,
                MealType.DINNER,
                "Soup",
                recipeId,
                null,
                null
        );

        assertThat(output.meal().shoppingHandledAt()).isEqualTo(handledAt);
        assertThat(output.meal().shoppingListId()).isEqualTo(listId);
    }

    @Test
    void addMealClearsShoppingHandledSnapshotWhenMealChangesWithoutShoppingReview() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        UUID listId = UUID.randomUUID();
        Instant handledAt = Instant.parse("2026-03-20T10:00:00Z");
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                mock(MealsShoppingPort.class),
                Clock.fixed(handledAt, ZoneOffset.UTC)
        );

        recipes.save(new Recipe(
                recipeId,
                groupId,
                "Soup",
                Instant.parse("2026-03-01T09:00:00Z"),
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(), "Tomato", null, null, 1))
        ));

        service.addOrReplaceMeal(groupId, userId, 2026, 12, 1, MealType.DINNER, recipeId, listId, List.of(1));

        MealsApplicationService clearingService = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                mock(MealsShoppingPort.class),
                Clock.fixed(handledAt.plusSeconds(3600), ZoneOffset.UTC)
        );
        var output = clearingService.addOrReplaceMeal(
                groupId,
                userId,
                2026,
                12,
                1,
                MealType.DINNER,
                "Soup with bread",
                recipeId,
                null,
                null
        );

        assertThat(output.meal().shoppingHandledAt()).isNull();
        assertThat(output.meal().shoppingListId()).isNull();
    }

    @Test
    void addMealFallsBackToRecipeNameWhenMealTitleIsOmittedForRecipeBackedMeal() {
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
                "Tomatsoppa",
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of()
        ));

        var output = service.addOrReplaceMeal(
                groupId,
                userId,
                2026,
                5,
                1,
                MealType.DINNER,
                null,
                recipeId,
                null,
                null
        );

        assertThat(output.meal().mealTitle()).isEqualTo("Tomatsoppa");
        assertThat(output.meal().recipeTitle()).isEqualTo("Tomatsoppa");
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
                "4 servings",
                "Best for weekends",
                "Mix ingredients\nBake for 20 minutes",
                true,
                List.of()
        );

        assertThat(created.sourceName()).isEqualTo("Grandma's notebook");
        assertThat(created.sourceUrl()).isEqualTo("https://example.com/grandma-recipe");
        assertThat(created.originKind()).isEqualTo("URL_IMPORT");
        assertThat(created.servings()).isEqualTo("4 servings");
        assertThat(created.shortNote()).isEqualTo("Best for weekends");
        assertThat(created.instructions()).isEqualTo("Mix ingredients\nBake for 20 minutes");
        assertThat(created.updatedAt()).isEqualTo(Instant.parse("2026-02-01T10:00:00Z"));
        assertThat(created.archivedAt()).isNull();
        assertThat(created.savedInRecipes()).isTrue();
    }

    @Test
    void updateRecipeCarriesServingsThroughSavedRecipeUpdates() {
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
                Clock.fixed(Instant.parse("2026-02-05T10:00:00Z"), ZoneOffset.UTC)
        );

        recipes.save(new Recipe(
                recipeId,
                groupId,
                "Soup",
                "Notebook",
                "https://example.com/soup",
                RecipeOriginKind.URL_IMPORT,
                "2 servings",
                "Initial note",
                "Cook",
                Instant.parse("2026-02-01T10:00:00Z"),
                Instant.parse("2026-02-01T10:00:00Z"),
                List.of()
        ));

        var updated = service.updateRecipe(
                groupId,
                userId,
                recipeId,
                "Soup",
                "Notebook",
                "https://example.com/soup",
                "URL_IMPORT",
                "4 servings",
                "Updated note",
                "Cook gently",
                true,
                List.of()
        );

        assertThat(updated.servings()).isEqualTo("4 servings");
        assertThat(updated.shortNote()).isEqualTo("Updated note");
        assertThat(updated.instructions()).isEqualTo("Cook gently");
    }

    @Test
    void createRecipeNormalizesIngredientNamesButPreservesRawText() {
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
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                List.of(new IngredientInput("  Olive   Oil  ", "2 tbsp olive oil", null, null, 1))
        );

        assertThat(created.ingredients()).hasSize(1);
        assertThat(created.ingredients().get(0).name()).isEqualTo("Olive Oil");
        assertThat(created.ingredients().get(0).rawText()).isEqualTo("2 tbsp olive oil");
        assertThat(created.savedInRecipes()).isFalse();
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
                true,
                List.of()
        ));

        var archivedRecipes = service.listArchivedRecipes(groupId, userId);

        assertThat(archivedRecipes).extracting(view -> view.name()).containsExactly("Archived Recipe");
    }

    @Test
    void listRecentlyUsedRecipesReturnsRecentActiveRecipesFromPlannedMeals() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID fridayRecipeId = UUID.randomUUID();
        UUID tuesdayRecipeId = UUID.randomUUID();
        UUID priorWeekRecipeId = UUID.randomUUID();
        UUID archivedRecipeId = UUID.randomUUID();
        UUID futureRecipeId = UUID.randomUUID();
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

        recipes.save(new Recipe(fridayRecipeId, groupId, "Friday Pasta", Instant.parse("2026-03-10T09:00:00Z"), List.of()));
        recipes.save(new Recipe(tuesdayRecipeId, groupId, "Tuesday Soup", Instant.parse("2026-03-09T09:00:00Z"), List.of()));
        recipes.save(new Recipe(priorWeekRecipeId, groupId, "Last Week Pie", Instant.parse("2026-03-08T09:00:00Z"), List.of()));
        recipes.save(new Recipe(
                archivedRecipeId,
                groupId,
                "Archived Stew",
                null,
                null,
                null,
                null,
                null,
                Instant.parse("2026-03-07T09:00:00Z"),
                Instant.parse("2026-03-18T10:00:00Z"),
                Instant.parse("2026-03-18T10:00:00Z"),
                true,
                List.of()
        ));
        recipes.save(new Recipe(futureRecipeId, groupId, "Future Curry", Instant.parse("2026-03-11T09:00:00Z"), List.of()));

        service.addOrReplaceMeal(groupId, userId, 2026, 12, 5, MealType.DINNER, fridayRecipeId, null, null);
        service.addOrReplaceMeal(groupId, userId, 2026, 12, 2, MealType.DINNER, tuesdayRecipeId, null, null);
        service.addOrReplaceMeal(groupId, userId, 2026, 11, 4, MealType.DINNER, priorWeekRecipeId, null, null);
        service.addOrReplaceMeal(groupId, userId, 2026, 12, 1, MealType.DINNER, archivedRecipeId, null, null);
        service.addOrReplaceMeal(groupId, userId, 2026, 13, 1, MealType.DINNER, futureRecipeId, null, null);

        var recentRecipes = service.listRecentlyUsedRecipes(groupId, userId);

        assertThat(recentRecipes).extracting(view -> view.name())
                .containsExactly("Friday Pasta", "Tuesday Soup", "Last Week Pie");
    }

    @Test
    void listRecentPlannedMealsReturnsDistinctReusableMealsFromHistory() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID pastaRecipeId = UUID.randomUUID();
        UUID soupRecipeId = UUID.randomUUID();
        UUID archivedRecipeId = UUID.randomUUID();
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

        recipes.save(new Recipe(pastaRecipeId, groupId, "Pasta Bake", Instant.parse("2026-03-10T09:00:00Z"), List.of()));
        recipes.save(new Recipe(soupRecipeId, groupId, "Tomato Soup", Instant.parse("2026-03-09T09:00:00Z"), List.of()));
        recipes.save(new Recipe(
                archivedRecipeId,
                groupId,
                "Old Curry",
                null,
                null,
                null,
                null,
                null,
                Instant.parse("2026-03-08T09:00:00Z"),
                Instant.parse("2026-03-18T10:00:00Z"),
                Instant.parse("2026-03-18T10:00:00Z"),
                true,
                List.of()
        ));

        service.addOrReplaceMeal(groupId, userId, 2026, 12, 5, MealType.DINNER, "Pasta Bake", pastaRecipeId, null, null);
        service.addOrReplaceMeal(groupId, userId, 2026, 12, 4, MealType.DINNER, "Pasta Bake", pastaRecipeId, null, null);
        service.addOrReplaceMeal(groupId, userId, 2026, 12, 3, MealType.LUNCH, "Soup lunch", soupRecipeId, null, null);
        service.addOrReplaceMeal(groupId, userId, 2026, 12, 2, MealType.DINNER, "Takeaway", null, null, null);
        service.addOrReplaceMeal(groupId, userId, 2026, 12, 1, MealType.DINNER, "Old Curry", archivedRecipeId, null, null);

        var recentMeals = service.listRecentPlannedMeals(groupId, userId);

        assertThat(recentMeals).extracting(view -> view.mealTitle())
                .containsExactly("Pasta Bake", "Soup lunch", "Takeaway", "Old Curry");
        assertThat(recentMeals.get(0).recipeId()).isEqualTo(pastaRecipeId);
        assertThat(recentMeals.get(1).recipeId()).isEqualTo(soupRecipeId);
        assertThat(recentMeals.get(2).recipeId()).isNull();
        assertThat(recentMeals.get(3).recipeId()).isNull();
    }

    @Test
    void markAndClearRecipeMakeSoonUpdatesRecipeState() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        MealsShoppingPort shopping = mock(MealsShoppingPort.class);
        Instant markedAt = Instant.parse("2026-03-20T10:00:00Z");
        MealsApplicationService markService = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                shopping,
                Clock.fixed(markedAt, ZoneOffset.UTC)
        );

        recipes.save(new Recipe(
                recipeId,
                groupId,
                "Pasta",
                Instant.parse("2026-03-18T09:00:00Z"),
                List.of()
        ));

        var marked = markService.markRecipeMakeSoon(groupId, userId, recipeId);

        assertThat(marked.makeSoonAt()).isEqualTo(markedAt);

        Instant clearedAt = Instant.parse("2026-03-21T08:00:00Z");
        MealsApplicationService clearService = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                shopping,
                Clock.fixed(clearedAt, ZoneOffset.UTC)
        );

        var cleared = clearService.clearRecipeMakeSoon(groupId, userId, recipeId);

        assertThat(cleared.makeSoonAt()).isNull();
        assertThat(cleared.updatedAt()).isEqualTo(clearedAt);
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
                true,
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
                true,
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
                true,
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
                true,
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
                true,
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
    void getWeekPlanMarksWeekShoppingReviewAsAvailableWhenWeekHasRecipeIngredients() {
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
                "Tacos",
                Instant.parse("2026-03-10T09:00:00Z"),
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(), "Egg", null, null, 1))
        ));

        service.addOrReplaceMeal(groupId, userId, 2026, 12, 1, MealType.DINNER, recipeId, null, null);

        var weekPlan = service.getWeekPlan(groupId, userId, 2026, 12);

        assertThat(weekPlan.hasReviewableWeekShopping()).isTrue();
    }

    @Test
    void getWeekPlanLeavesWeekShoppingReviewUnavailableWhenWeekHasNoReviewableIngredients() {
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
                "Empty Recipe",
                Instant.parse("2026-03-10T09:00:00Z"),
                List.of()
        ));

        service.addOrReplaceMeal(groupId, userId, 2026, 12, 1, MealType.DINNER, recipeId, null, null);
        service.addOrReplaceMeal(groupId, userId, 2026, 12, 2, MealType.LUNCH, "Soup", null, null, null);

        var weekPlan = service.getWeekPlan(groupId, userId, 2026, 12);

        assertThat(weekPlan.hasReviewableWeekShopping()).isFalse();
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

    @Test
    void program3FoundationBuildsMealShoppingProjectionFromLinkedShoppingState() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        Clock clock = Clock.fixed(Instant.parse("2026-03-24T10:00:00Z"), ZoneOffset.UTC);
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        ShoppingApplicationService shoppingService = new ShoppingApplicationService(
                new InMemoryShoppingListRepository(),
                new InMemoryShoppingCategoryPreferenceRepository(),
                membership,
                clock
        );
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                new MealsShoppingPortAdapter(shoppingService),
                clock
        );

        recipes.save(new Recipe(
                recipeId,
                groupId,
                "Creamy Pasta",
                Instant.parse("2026-03-01T09:00:00Z"),
                List.of(
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.randomUUID(), "Pasta", null, null, null, 1
                        ),
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.randomUUID(), "Milk", null, new BigDecimal("1"), IngredientUnit.PACK, 2
                        )
                )
        ));

        UUID listId = shoppingService.createShoppingList(groupId, userId, "Main list", ShoppingListType.MIXED).listId();
        service.addOrReplaceMeal(groupId, userId, 2026, 13, 2, MealType.DINNER, recipeId, listId, null);

        UUID milkItemId = shoppingService.listShoppingLists(groupId, userId).stream()
                .filter(list -> list.id().equals(listId))
                .flatMap(list -> list.items().stream())
                .filter(item -> item.name().equals("milk"))
                .findFirst()
                .orElseThrow()
                .id();
        shoppingService.toggleShoppingItem(groupId, userId, listId, milkItemId);

        var projection = service.getMealShoppingProjection(groupId, userId, 2026, 13, 2, MealType.DINNER, null);

        assertThat(projection.shoppingLink().status()).isEqualTo("linked");
        assertThat(projection.delta().unresolvedIngredientCount()).isZero();
        assertThat(projection.readiness().state()).isEqualTo("partially_ready");
        assertThat(projection.ingredientCoverage()).anySatisfy(coverage -> {
            assertThat(coverage.need().normalizedShoppingName()).isEqualTo("pasta");
            assertThat(coverage.coverageState()).isEqualTo("covered");
            assertThat(coverage.shoppingState()).isEqualTo("to_buy");
        });
        assertThat(projection.ingredientCoverage()).anySatisfy(coverage -> {
            assertThat(coverage.need().normalizedShoppingName()).isEqualTo("milk");
            assertThat(coverage.coverageState()).isEqualTo("covered");
            assertThat(coverage.shoppingState()).isEqualTo("bought");
        });
    }

    @Test
    void program3FoundationCanAssessMealAgainstSelectedShoppingListBeforeLinking() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        Clock clock = Clock.fixed(Instant.parse("2026-03-24T10:00:00Z"), ZoneOffset.UTC);
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        ShoppingApplicationService shoppingService = new ShoppingApplicationService(
                new InMemoryShoppingListRepository(),
                new InMemoryShoppingCategoryPreferenceRepository(),
                membership,
                clock
        );
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                new MealsShoppingPortAdapter(shoppingService),
                clock
        );

        recipes.save(new Recipe(
                recipeId,
                groupId,
                "Creamy Pasta",
                Instant.parse("2026-03-01T09:00:00Z"),
                List.of(
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.randomUUID(), "Pasta", null, new BigDecimal("200"), IngredientUnit.G, 1
                        ),
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.randomUUID(), "Milk", null, new BigDecimal("1"), IngredientUnit.PACK, 2
                        )
                )
        ));

        UUID listId = shoppingService.createShoppingList(groupId, userId, "Main list", ShoppingListType.MIXED).listId();
        service.addOrReplaceMeal(groupId, userId, 2026, 13, 2, MealType.DINNER, recipeId, null, null);
        shoppingService.addShoppingItem(groupId, userId, listId, "pasta", null, null, null, null);

        var projection = service.getMealShoppingProjection(groupId, userId, 2026, 13, 2, MealType.DINNER, listId);

        assertThat(projection.shoppingLink().status()).isEqualTo("not_linked");
        assertThat(projection.shoppingLink().shoppingListId()).isNull();
        assertThat(projection.ingredientCoverage()).anySatisfy(coverage -> {
            assertThat(coverage.need().normalizedShoppingName()).isEqualTo("pasta");
            assertThat(coverage.coverageState()).isEqualTo("unknown");
            assertThat(coverage.shoppingState()).isEqualTo("to_buy");
        });
        assertThat(projection.ingredientCoverage()).anySatisfy(coverage -> {
            assertThat(coverage.need().normalizedShoppingName()).isEqualTo("milk");
            assertThat(coverage.coverageState()).isEqualTo("missing");
        });
    }

    @Test
    void program3FoundationRecognizesMatchingIngredientAcrossMealsOnSameSelectedList() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID pancakeRecipeId = UUID.randomUUID();
        UUID omeletteRecipeId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        Clock clock = Clock.fixed(Instant.parse("2026-03-24T10:00:00Z"), ZoneOffset.UTC);
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        ShoppingApplicationService shoppingService = new ShoppingApplicationService(
                new InMemoryShoppingListRepository(),
                new InMemoryShoppingCategoryPreferenceRepository(),
                membership,
                clock
        );
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                new MealsShoppingPortAdapter(shoppingService),
                clock
        );

        recipes.save(new Recipe(
                pancakeRecipeId,
                groupId,
                "Pancakes",
                Instant.parse("2026-03-01T09:00:00Z"),
                List.of(
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.randomUUID(), "Egg", null, null, null, 1
                        ),
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.randomUUID(), "Milk", null, null, null, 2
                        )
                )
        ));
        recipes.save(new Recipe(
                omeletteRecipeId,
                groupId,
                "Omelette",
                Instant.parse("2026-03-02T09:00:00Z"),
                List.of(
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.randomUUID(), "Egg", null, null, null, 1
                        ),
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.randomUUID(), "Butter", null, null, null, 2
                        )
                )
        ));

        UUID listId = shoppingService.createShoppingList(groupId, userId, "Main list", ShoppingListType.MIXED).listId();

        service.addOrReplaceMeal(groupId, userId, 2026, 13, 2, MealType.DINNER, pancakeRecipeId, listId, null);
        service.addOrReplaceMeal(groupId, userId, 2026, 13, 3, MealType.DINNER, omeletteRecipeId, null, null);

        var projection = service.getMealShoppingProjection(groupId, userId, 2026, 13, 3, MealType.DINNER, listId);

        assertThat(projection.shoppingLink().status()).isEqualTo("not_linked");
        assertThat(projection.ingredientCoverage()).anySatisfy(coverage -> {
            assertThat(coverage.need().normalizedShoppingName()).isEqualTo("egg");
            assertThat(coverage.matchingItemCount()).isEqualTo(1);
            assertThat(coverage.coverageState()).isEqualTo("covered");
        });
        assertThat(projection.ingredientCoverage()).anySatisfy(coverage -> {
            assertThat(coverage.need().normalizedShoppingName()).isEqualTo("butter");
            assertThat(coverage.matchingItemCount()).isZero();
            assertThat(coverage.coverageState()).isEqualTo("missing");
        });
    }

    @Test
    void program3FoundationKeepsTitleOnlyMealsAsReadinessUnclear() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                mock(MealsShoppingPort.class),
                Clock.fixed(Instant.parse("2026-03-24T10:00:00Z"), ZoneOffset.UTC)
        );

        service.addOrReplaceMeal(groupId, userId, 2026, 13, 4, MealType.DINNER, "Leftovers", null, null, null);

        var projection = service.getMealShoppingProjection(groupId, userId, 2026, 13, 4, MealType.DINNER, null);

        assertThat(projection.recipeBacked()).isFalse();
        assertThat(projection.shoppingLink().status()).isEqualTo("not_linked");
        assertThat(projection.readiness().state()).isEqualTo("readiness_unclear");
        assertThat(projection.ingredientCoverage()).isEmpty();
    }

    @Test
    void program3FoundationBuildsWeekShoppingProjectionAcrossNeedsAndUnclearMeals() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        Clock clock = Clock.fixed(Instant.parse("2026-03-24T10:00:00Z"), ZoneOffset.UTC);
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                mock(MealsShoppingPort.class),
                clock
        );

        recipes.save(new Recipe(
                recipeId,
                groupId,
                "Tomato Soup",
                Instant.parse("2026-03-01T09:00:00Z"),
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(), "Tomatoes", null, new BigDecimal("2"), IngredientUnit.PCS, 1
                ))
        ));

        service.addOrReplaceMeal(groupId, userId, 2026, 13, 1, MealType.DINNER, recipeId, null, null);
        service.addOrReplaceMeal(groupId, userId, 2026, 13, 2, MealType.DINNER, "Sandwiches", null, null, null);

        var projection = service.getWeekShoppingProjection(groupId, userId, 2026, 13);

        assertThat(projection.mealsNeedingShoppingCount()).isEqualTo(1);
        assertThat(projection.readinessUnclearMealCount()).isEqualTo(1);
        assertThat(projection.delta().unresolvedIngredientCount()).isEqualTo(1);
        assertThat(projection.meals()).anySatisfy(meal -> {
            assertThat(meal.mealTitle()).isEqualTo("Tomato Soup");
            assertThat(meal.readiness().state()).isEqualTo("needs_shopping");
        });
    }

    @Test
    void program4FoundationBuildsWeekShoppingReviewFromAggregatedWeekNeeds() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID pancakesRecipeId = UUID.randomUUID();
        UUID omeletteRecipeId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        Clock clock = Clock.fixed(Instant.parse("2026-03-24T10:00:00Z"), ZoneOffset.UTC);
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        ShoppingApplicationService shoppingService = new ShoppingApplicationService(
                new InMemoryShoppingListRepository(),
                new InMemoryShoppingCategoryPreferenceRepository(),
                membership,
                clock
        );
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                new MealsShoppingPortAdapter(shoppingService),
                clock
        );

        recipes.save(new Recipe(
                pancakesRecipeId,
                groupId,
                "Pancakes",
                Instant.parse("2026-03-01T09:00:00Z"),
                List.of(
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.randomUUID(), "Egg", null, new BigDecimal("3"), IngredientUnit.PCS, 1
                        ),
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.randomUUID(), "Milk", null, new BigDecimal("2"), IngredientUnit.DL, 2
                        )
                )
        ));
        recipes.save(new Recipe(
                omeletteRecipeId,
                groupId,
                "Omelette",
                Instant.parse("2026-03-02T09:00:00Z"),
                List.of(
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.randomUUID(), "Egg", null, new BigDecimal("5"), IngredientUnit.PCS, 1
                        ),
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.randomUUID(), "Milk", null, new BigDecimal("4"), IngredientUnit.DL, 2
                        )
                )
        ));

        service.addOrReplaceMeal(groupId, userId, 2026, 13, 2, MealType.DINNER, pancakesRecipeId, null, null);
        service.addOrReplaceMeal(groupId, userId, 2026, 13, 3, MealType.DINNER, omeletteRecipeId, null, null);

        UUID listId = shoppingService.createShoppingList(groupId, userId, "Weekly groceries", ShoppingListType.GROCERY).listId();
        shoppingService.addShoppingItem(groupId, userId, listId, "egg", new BigDecimal("4"), ShoppingUnit.PCS);

        var review = service.getWeekShoppingReview(groupId, userId, 2026, 13, listId);

        assertThat(review.assessedShoppingListId()).isEqualTo(listId);
        assertThat(review.assessedShoppingListName()).isEqualTo("Weekly groceries");
        assertThat(review.ingredients()).hasSize(2);
        assertThat(review.ingredients()).anySatisfy(ingredient -> {
            assertThat(ingredient.need().normalizedShoppingName()).isEqualTo("egg");
            assertThat(ingredient.need().totalQuantity()).isEqualByComparingTo("8");
            assertThat(ingredient.need().unitName()).isEqualTo("PCS");
            assertThat(ingredient.comparisonState()).isEqualTo("add_to_list");
            assertThat(ingredient.quantityOnList()).isEqualByComparingTo("4");
            assertThat(ingredient.remainingQuantity()).isEqualByComparingTo("4");
            assertThat(ingredient.need().contributors()).hasSize(2);
        });
        assertThat(review.ingredients()).anySatisfy(ingredient -> {
            assertThat(ingredient.need().normalizedShoppingName()).isEqualTo("milk");
            assertThat(ingredient.need().totalQuantity()).isEqualByComparingTo("6");
            assertThat(ingredient.need().unitName()).isEqualTo("DL");
            assertThat(ingredient.comparisonState()).isEqualTo("add_to_list");
            assertThat(ingredient.quantityOnList()).isEqualByComparingTo("0");
            assertThat(ingredient.remainingQuantity()).isEqualByComparingTo("6");
        });
    }

    @Test
    void program4FoundationKeepsIncompatibleUnitsAsSeparateWeekReviewLines() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeOneId = UUID.randomUUID();
        UUID recipeTwoId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        Clock clock = Clock.fixed(Instant.parse("2026-03-24T10:00:00Z"), ZoneOffset.UTC);
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                mock(MealsShoppingPort.class),
                clock
        );

        recipes.save(new Recipe(
                recipeOneId,
                groupId,
                "Bread",
                Instant.parse("2026-03-01T09:00:00Z"),
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(), "Flour", null, new BigDecimal("500"), IngredientUnit.G, 1
                ))
        ));
        recipes.save(new Recipe(
                recipeTwoId,
                groupId,
                "Cake",
                Instant.parse("2026-03-02T09:00:00Z"),
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(), "Flour", null, new BigDecimal("1"), IngredientUnit.KG, 1
                ))
        ));

        service.addOrReplaceMeal(groupId, userId, 2026, 13, 2, MealType.DINNER, recipeOneId, null, null);
        service.addOrReplaceMeal(groupId, userId, 2026, 13, 3, MealType.DINNER, recipeTwoId, null, null);

        var review = service.getWeekShoppingReview(groupId, userId, 2026, 13, null);

        assertThat(review.ingredients())
                .filteredOn(ingredient -> ingredient.need().normalizedShoppingName().equals("flour"))
                .singleElement()
                .satisfies(ingredient -> {
                    assertThat(ingredient.need().quantityConfidence()).isEqualTo("uncertain");
                    assertThat(ingredient.need().totalQuantity()).isNull();
                    assertThat(ingredient.need().unitName()).isNull();
                    assertThat(ingredient.need().contributors()).hasSize(2);
                });
    }

    @Test
    void program4IdentityFirstMergesExactSameUnitBananaQuantities() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID smoothieRecipeId = UUID.randomUUID();
        UUID pancakesRecipeId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        Clock clock = Clock.fixed(Instant.parse("2026-03-24T10:00:00Z"), ZoneOffset.UTC);
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                mock(MealsShoppingPort.class),
                clock
        );

        recipes.save(new Recipe(
                smoothieRecipeId,
                groupId,
                "Smoothie",
                Instant.parse("2026-03-01T09:00:00Z"),
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(), "Banana", null, new BigDecimal("1"), IngredientUnit.PCS, 1
                ))
        ));
        recipes.save(new Recipe(
                pancakesRecipeId,
                groupId,
                "Pancakes",
                Instant.parse("2026-03-02T09:00:00Z"),
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(), "Banana", null, new BigDecimal("2"), IngredientUnit.PCS, 1
                ))
        ));

        service.addOrReplaceMeal(groupId, userId, 2026, 13, 2, MealType.DINNER, smoothieRecipeId, null, null);
        service.addOrReplaceMeal(groupId, userId, 2026, 13, 3, MealType.DINNER, pancakesRecipeId, null, null);

        var review = service.getWeekShoppingReview(groupId, userId, 2026, 13, null);

        assertThat(review.ingredients())
                .filteredOn(ingredient -> ingredient.need().normalizedShoppingName().equals("banana"))
                .singleElement()
                .satisfies(ingredient -> {
                    assertThat(ingredient.need().quantityConfidence()).isEqualTo("exact");
                    assertThat(ingredient.need().totalQuantity()).isEqualByComparingTo("3");
                    assertThat(ingredient.need().unitName()).isEqualTo("PCS");
                    assertThat(ingredient.need().contributors()).hasSize(2);
                });
    }

    @Test
    void program4IdentityFirstMergesBananaWithMixedQuantityConfidence() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID tacosRecipeId = UUID.randomUUID();
        UUID bananaRecipeId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        Clock clock = Clock.fixed(Instant.parse("2026-03-24T10:00:00Z"), ZoneOffset.UTC);
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                mock(MealsShoppingPort.class),
                clock
        );

        recipes.save(new Recipe(
                tacosRecipeId,
                groupId,
                "Tacos",
                Instant.parse("2026-03-01T09:00:00Z"),
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(), "Banana", "banana", null, null, 1
                ))
        ));
        recipes.save(new Recipe(
                bananaRecipeId,
                groupId,
                "Banana bread",
                Instant.parse("2026-03-02T09:00:00Z"),
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(), "Banana", null, new BigDecimal("2"), IngredientUnit.PCS, 1
                ))
        ));

        service.addOrReplaceMeal(groupId, userId, 2026, 13, 2, MealType.DINNER, tacosRecipeId, null, null);
        service.addOrReplaceMeal(groupId, userId, 2026, 13, 3, MealType.DINNER, bananaRecipeId, null, null);

        var review = service.getWeekShoppingReview(groupId, userId, 2026, 13, null);

        assertThat(review.ingredients())
                .filteredOn(ingredient -> ingredient.need().normalizedShoppingName().equals("banana"))
                .singleElement()
                .satisfies(ingredient -> {
                    assertThat(ingredient.need().quantityConfidence()).isEqualTo("uncertain");
                    assertThat(ingredient.need().totalQuantity()).isNull();
                    assertThat(ingredient.need().unitName()).isNull();
                    assertThat(ingredient.need().contributors()).hasSize(2);
                });
    }

    @Test
    void program4IdentityFirstMergesSaltAndMeasuredSaltWithoutFalsePrecision() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID tacosRecipeId = UUID.randomUUID();
        UUID soupRecipeId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        Clock clock = Clock.fixed(Instant.parse("2026-03-24T10:00:00Z"), ZoneOffset.UTC);
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                mock(MealsShoppingPort.class),
                clock
        );

        recipes.save(new Recipe(
                tacosRecipeId,
                groupId,
                "Tacos",
                Instant.parse("2026-03-01T09:00:00Z"),
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(), "Salt", "salt", null, null, 1
                ))
        ));
        recipes.save(new Recipe(
                soupRecipeId,
                groupId,
                "Soup",
                Instant.parse("2026-03-02T09:00:00Z"),
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(), "Salt", null, new BigDecimal("15"), IngredientUnit.ML, 1
                ))
        ));

        service.addOrReplaceMeal(groupId, userId, 2026, 13, 2, MealType.DINNER, tacosRecipeId, null, null);
        service.addOrReplaceMeal(groupId, userId, 2026, 13, 3, MealType.DINNER, soupRecipeId, null, null);

        var review = service.getWeekShoppingReview(groupId, userId, 2026, 13, null);

        assertThat(review.ingredients())
                .filteredOn(ingredient -> ingredient.need().normalizedShoppingName().equals("salt"))
                .singleElement()
                .satisfies(ingredient -> {
                    assertThat(ingredient.need().quantityConfidence()).isEqualTo("uncertain");
                    assertThat(ingredient.need().totalQuantity()).isNull();
                    assertThat(ingredient.need().unitName()).isNull();
                    assertThat(ingredient.need().contributors()).hasSize(2);
                });
    }

    @Test
    void program4IdentityFirstMergesOliveOilWithoutFalsePrecision() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID saladRecipeId = UUID.randomUUID();
        UUID pastaRecipeId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        Clock clock = Clock.fixed(Instant.parse("2026-03-24T10:00:00Z"), ZoneOffset.UTC);
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                mock(MealsShoppingPort.class),
                clock
        );

        recipes.save(new Recipe(
                saladRecipeId,
                groupId,
                "Salad",
                Instant.parse("2026-03-01T09:00:00Z"),
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(), "Olive oil", "olive oil", null, null, 1
                ))
        ));
        recipes.save(new Recipe(
                pastaRecipeId,
                groupId,
                "Pasta",
                Instant.parse("2026-03-02T09:00:00Z"),
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(), "Olive oil", null, new BigDecimal("30"), IngredientUnit.ML, 1
                ))
        ));

        service.addOrReplaceMeal(groupId, userId, 2026, 13, 2, MealType.DINNER, saladRecipeId, null, null);
        service.addOrReplaceMeal(groupId, userId, 2026, 13, 3, MealType.DINNER, pastaRecipeId, null, null);

        var review = service.getWeekShoppingReview(groupId, userId, 2026, 13, null);

        assertThat(review.ingredients())
                .filteredOn(ingredient -> ingredient.need().normalizedShoppingName().equals("olive oil"))
                .singleElement()
                .satisfies(ingredient -> {
                    assertThat(ingredient.need().quantityConfidence()).isEqualTo("uncertain");
                    assertThat(ingredient.need().totalQuantity()).isNull();
                    assertThat(ingredient.need().unitName()).isNull();
                    assertThat(ingredient.need().contributors()).hasSize(2);
                });
    }

    @Test
    void mealPlanShoppingMergeDropsFalsePrecisionForUncertainSaltIdentity() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        Clock clock = Clock.fixed(Instant.parse("2026-03-24T10:00:00Z"), ZoneOffset.UTC);
        ShoppingApplicationService shoppingService = new ShoppingApplicationService(
                new InMemoryShoppingListRepository(),
                new InMemoryShoppingCategoryPreferenceRepository(),
                membership,
                clock
        );

        UUID listId = shoppingService.createShoppingList(groupId, userId, "Weekly groceries", ShoppingListType.GROCERY).listId();
        shoppingService.addShoppingItem(groupId, userId, listId, "salt", new BigDecimal("15"), ShoppingUnit.ML);
        shoppingService.addShoppingItem(
                groupId,
                userId,
                listId,
                "salt",
                null,
                null,
                app.lifelinq.features.shopping.domain.ShoppingItemSourceKind.MEAL_PLAN,
                "Week 13 meals"
        );

        var shoppingLists = shoppingService.listShoppingLists(groupId, userId);
        assertThat(shoppingLists).singleElement().satisfies(list -> {
            assertThat(list.items()).singleElement().satisfies(item -> {
                assertThat(item.name()).isEqualTo("salt");
                assertThat(item.quantity()).isNull();
                assertThat(item.unit()).isNull();
            });
        });
    }

    @Test
    void program4CorrectionKeepsWeekReviewLineIdsStableAcrossListAssessmentChanges() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        Clock clock = Clock.fixed(Instant.parse("2026-03-24T10:00:00Z"), ZoneOffset.UTC);
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        ShoppingApplicationService shoppingService = new ShoppingApplicationService(
                new InMemoryShoppingListRepository(),
                new InMemoryShoppingCategoryPreferenceRepository(),
                membership,
                clock
        );
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                new MealsShoppingPortAdapter(shoppingService),
                clock
        );

        recipes.save(new Recipe(
                recipeId,
                groupId,
                "Plattar",
                Instant.parse("2026-03-01T09:00:00Z"),
                List.of(
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.randomUUID(), "Egg", null, new BigDecimal("2"), IngredientUnit.PCS, 1
                        ),
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.randomUUID(), "Milk", null, new BigDecimal("6"), IngredientUnit.DL, 2
                        )
                )
        ));

        service.addOrReplaceMeal(groupId, userId, 2026, 13, 2, MealType.DINNER, recipeId, null, null);

        UUID firstListId = shoppingService.createShoppingList(groupId, userId, "Weekly groceries", ShoppingListType.GROCERY).listId();
        UUID secondListId = shoppingService.createShoppingList(groupId, userId, "Cabin supplies", ShoppingListType.GROCERY).listId();

        var firstReview = service.getWeekShoppingReview(groupId, userId, 2026, 13, firstListId);
        var secondReview = service.getWeekShoppingReview(groupId, userId, 2026, 13, secondListId);

        assertThat(firstReview.ingredients())
                .extracting(ingredient -> ingredient.need().lineId())
                .containsExactlyElementsOf(
                        secondReview.ingredients().stream()
                                .map(ingredient -> ingredient.need().lineId())
                                .toList()
                );
    }

    @Test
    void program4CorrectionMarksFullyCoveredAggregatedWeekLinesAsAlreadyOnList() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID pancakesRecipeId = UUID.randomUUID();
        UUID omeletteRecipeId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        Clock clock = Clock.fixed(Instant.parse("2026-03-24T10:00:00Z"), ZoneOffset.UTC);
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        ShoppingApplicationService shoppingService = new ShoppingApplicationService(
                new InMemoryShoppingListRepository(),
                new InMemoryShoppingCategoryPreferenceRepository(),
                membership,
                clock
        );
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                new MealsShoppingPortAdapter(shoppingService),
                clock
        );

        recipes.save(new Recipe(
                pancakesRecipeId,
                groupId,
                "Pancakes",
                Instant.parse("2026-03-01T09:00:00Z"),
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(), "Egg", null, new BigDecimal("5"), IngredientUnit.PCS, 1
                ))
        ));
        recipes.save(new Recipe(
                omeletteRecipeId,
                groupId,
                "Omelette",
                Instant.parse("2026-03-02T09:00:00Z"),
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(), "Egg", null, new BigDecimal("3"), IngredientUnit.PCS, 1
                ))
        ));

        service.addOrReplaceMeal(groupId, userId, 2026, 13, 2, MealType.DINNER, pancakesRecipeId, null, null);
        service.addOrReplaceMeal(groupId, userId, 2026, 13, 3, MealType.DINNER, omeletteRecipeId, null, null);

        UUID listId = shoppingService.createShoppingList(groupId, userId, "Weekly groceries", ShoppingListType.GROCERY).listId();
        shoppingService.addShoppingItem(groupId, userId, listId, "egg", new BigDecimal("8"), ShoppingUnit.PCS);

        var review = service.getWeekShoppingReview(groupId, userId, 2026, 13, listId);

        assertThat(review.ingredients()).singleElement().satisfies(ingredient -> {
            assertThat(ingredient.need().normalizedShoppingName()).isEqualTo("egg");
            assertThat(ingredient.comparisonState()).isEqualTo("already_on_list");
            assertThat(ingredient.remainingQuantity()).isNull();
            assertThat(ingredient.quantityOnList()).isEqualByComparingTo("8");
        });
    }

    @Test
    void program4FoundationAddsOnlySelectedWeekReviewLinesAndRemembersWeekList() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID pancakesRecipeId = UUID.randomUUID();
        UUID omeletteRecipeId = UUID.randomUUID();
        EnsureGroupMemberUseCase membership = (h, u) -> {};
        Clock clock = Clock.fixed(Instant.parse("2026-03-24T10:00:00Z"), ZoneOffset.UTC);
        InMemoryWeekPlanRepository weekPlans = new InMemoryWeekPlanRepository();
        InMemoryRecipeRepository recipes = new InMemoryRecipeRepository();
        ShoppingApplicationService shoppingService = new ShoppingApplicationService(
                new InMemoryShoppingListRepository(),
                new InMemoryShoppingCategoryPreferenceRepository(),
                membership,
                clock
        );
        MealsApplicationService service = new MealsApplicationService(
                weekPlans,
                recipes,
                membership,
                new MealsShoppingPortAdapter(shoppingService),
                clock
        );

        recipes.save(new Recipe(
                pancakesRecipeId,
                groupId,
                "Pancakes",
                Instant.parse("2026-03-01T09:00:00Z"),
                List.of(
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.randomUUID(), "Egg", null, new BigDecimal("4"), IngredientUnit.PCS, 1
                        ),
                        new app.lifelinq.features.meals.domain.Ingredient(
                                UUID.randomUUID(), "Milk", null, new BigDecimal("6"), IngredientUnit.DL, 2
                        )
                )
        ));
        recipes.save(new Recipe(
                omeletteRecipeId,
                groupId,
                "Omelette",
                Instant.parse("2026-03-02T09:00:00Z"),
                List.of(new app.lifelinq.features.meals.domain.Ingredient(
                        UUID.randomUUID(), "Egg", null, new BigDecimal("2"), IngredientUnit.PCS, 1
                ))
        ));

        service.addOrReplaceMeal(groupId, userId, 2026, 13, 2, MealType.DINNER, pancakesRecipeId, null, null);
        service.addOrReplaceMeal(groupId, userId, 2026, 13, 3, MealType.DINNER, omeletteRecipeId, null, null);

        UUID listId = shoppingService.createShoppingList(groupId, userId, "Weekly groceries", ShoppingListType.GROCERY).listId();
        var review = service.getWeekShoppingReview(groupId, userId, 2026, 13, listId);
        String eggLineId = review.ingredients().stream()
                .filter(ingredient -> ingredient.need().normalizedShoppingName().equals("egg"))
                .findFirst()
                .orElseThrow()
                .need()
                .lineId();

        var updatedReview = service.addWeekShoppingReviewLines(
                groupId,
                userId,
                2026,
                13,
                listId,
                List.of(eggLineId)
        );

        var shoppingLists = shoppingService.listShoppingLists(groupId, userId);
        assertThat(shoppingLists).singleElement().satisfies(list -> {
            assertThat(list.id()).isEqualTo(listId);
            assertThat(list.items()).singleElement().satisfies(item -> {
                assertThat(item.name()).isEqualTo("egg");
                assertThat(item.quantity()).isEqualByComparingTo("6");
                assertThat(item.unit().name()).isEqualTo("PCS");
            });
        });

        assertThat(updatedReview.assessedShoppingListId()).isEqualTo(listId);
        assertThat(updatedReview.reviewLink()).isNotNull();
        assertThat(updatedReview.reviewLink().shoppingListId()).isEqualTo(listId);
        assertThat(updatedReview.ingredients()).anySatisfy(ingredient -> {
            assertThat(ingredient.need().normalizedShoppingName()).isEqualTo("egg");
            assertThat(ingredient.comparisonState()).isEqualTo("already_on_list");
        });
        assertThat(updatedReview.ingredients()).anySatisfy(ingredient -> {
            assertThat(ingredient.need().normalizedShoppingName()).isEqualTo("milk");
            assertThat(ingredient.comparisonState()).isEqualTo("add_to_list");
        });

        var reopenedReview = service.getWeekShoppingReview(groupId, userId, 2026, 13, null);
        assertThat(reopenedReview.assessedShoppingListId()).isEqualTo(listId);
        assertThat(reopenedReview.reviewLink()).isNotNull();
        assertThat(reopenedReview.reviewLink().shoppingListId()).isEqualTo(listId);
    }

    private static final class InMemoryWeekPlanRepository implements WeekPlanRepository, MealMemoryRepository {
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
                    .anyMatch(meal -> recipeId.equals(meal.getRecipeId()));
        }

        @Override
        public List<UUID> findRecentRecipeIdsOnOrBefore(UUID groupId, int year, int isoWeek, int dayOfWeek) {
            return byId.values().stream()
                    .filter(plan -> plan.getGroupId().equals(groupId))
                    .flatMap(plan -> plan.getMeals().stream()
                            .filter(meal -> meal.getRecipeId() != null)
                            .filter(meal -> plan.getYear() < year
                                    || (plan.getYear() == year && plan.getIsoWeek() < isoWeek)
                                    || (plan.getYear() == year && plan.getIsoWeek() == isoWeek && meal.getDayOfWeek() <= dayOfWeek))
                            .map(meal -> new RecentMealRecipe(plan.getYear(), plan.getIsoWeek(), meal.getDayOfWeek(), meal.getRecipeId())))
                    .sorted(Comparator
                            .comparingInt(RecentMealRecipe::year).reversed()
                            .thenComparing(Comparator.comparingInt(RecentMealRecipe::isoWeek).reversed())
                            .thenComparing(Comparator.comparingInt(RecentMealRecipe::dayOfWeek).reversed()))
                    .map(RecentMealRecipe::recipeId)
                    .toList();
        }

        @Override
        public List<RecentPlannedMeal> findRecentMealsOnOrBefore(UUID groupId, int year, int isoWeek, int dayOfWeek) {
            return byId.values().stream()
                    .filter(plan -> plan.getGroupId().equals(groupId))
                    .flatMap(plan -> plan.getMeals().stream()
                            .filter(meal -> plan.getYear() < year
                                    || (plan.getYear() == year && plan.getIsoWeek() < isoWeek)
                                    || (plan.getYear() == year && plan.getIsoWeek() == isoWeek && meal.getDayOfWeek() <= dayOfWeek))
                            .map(meal -> new RecentPlannedMeal(
                                    plan.getYear(),
                                    plan.getIsoWeek(),
                                    meal.getDayOfWeek(),
                                    meal.getMealType(),
                                    meal.getMealTitle(),
                                    meal.getRecipeId(),
                                    meal.getRecipeTitleSnapshot()
                            )))
                    .sorted(Comparator
                            .comparingInt(RecentPlannedMeal::year).reversed()
                            .thenComparing(Comparator.comparingInt(RecentPlannedMeal::isoWeek).reversed())
                            .thenComparing(Comparator.comparingInt(RecentPlannedMeal::dayOfWeek).reversed())
                            .thenComparing(meal -> meal.mealType().ordinal()))
                    .toList();
        }

        @Override
        public List<MealOccurrence> findHistoricalOccurrencesOnOrBefore(UUID groupId, int year, int isoWeek, int dayOfWeek) {
            return byId.values().stream()
                    .filter(plan -> plan.getGroupId().equals(groupId))
                    .flatMap(plan -> plan.getMeals().stream()
                            .filter(meal -> plan.getYear() < year
                                    || (plan.getYear() == year && plan.getIsoWeek() < isoWeek)
                                    || (plan.getYear() == year && plan.getIsoWeek() == isoWeek && meal.getDayOfWeek() <= dayOfWeek))
                            .map(meal -> new MealOccurrence(
                                    plan.getId(),
                                    plan.getYear(),
                                    plan.getIsoWeek(),
                                    meal.getDayOfWeek(),
                                    meal.getMealType(),
                                    LocalDate.of(plan.getYear(), 1, 4)
                                            .with(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear(), plan.getIsoWeek())
                                            .with(java.time.temporal.WeekFields.ISO.dayOfWeek(), meal.getDayOfWeek()),
                                    meal.getMealTitle(),
                                    meal.getRecipeId(),
                                    meal.getRecipeTitleSnapshot()
                            )))
                    .sorted(Comparator
                            .comparing(MealOccurrence::plannedDate).reversed()
                            .thenComparing(meal -> meal.mealType().ordinal()))
                    .toList();
        }
    }

    private record RecentMealRecipe(int year, int isoWeek, int dayOfWeek, UUID recipeId) {
    }

    private static final class InMemoryHouseholdPreferenceSignalRepository implements HouseholdPreferenceSignalRepository {
        private final Map<UUID, HouseholdPreferenceSignal> byId = new HashMap<>();

        @Override
        public HouseholdPreferenceSignal save(HouseholdPreferenceSignal signal) {
            byId.put(signal.getId(), signal);
            return signal;
        }

        @Override
        public List<HouseholdPreferenceSignal> findByGroupId(UUID groupId) {
            return byId.values().stream()
                    .filter(signal -> signal.getGroupId().equals(groupId))
                    .sorted(Comparator.comparing(HouseholdPreferenceSignal::getCreatedAt))
                    .toList();
        }

        @Override
        public Optional<HouseholdPreferenceSignal> findByRecipeTarget(
                UUID groupId,
                UUID recipeId,
                HouseholdPreferenceSignalType signalType
        ) {
            return byId.values().stream()
                    .filter(signal -> signal.getGroupId().equals(groupId))
                    .filter(signal -> signal.getTargetKind() == HouseholdPreferenceSignalTargetKind.RECIPE)
                    .filter(signal -> recipeId.equals(signal.getRecipeId()))
                    .filter(signal -> signal.getSignalType() == signalType)
                    .findFirst();
        }

        @Override
        public Optional<HouseholdPreferenceSignal> findByMealIdentityTarget(
                UUID groupId,
                String mealIdentityKey,
                HouseholdPreferenceSignalType signalType
        ) {
            return byId.values().stream()
                    .filter(signal -> signal.getGroupId().equals(groupId))
                    .filter(signal -> signal.getTargetKind() == HouseholdPreferenceSignalTargetKind.MEAL_IDENTITY)
                    .filter(signal -> mealIdentityKey.equals(signal.getMealIdentityKey()))
                    .filter(signal -> signal.getSignalType() == signalType)
                    .findFirst();
        }

        @Override
        public void delete(HouseholdPreferenceSignal signal) {
            byId.remove(signal.getId());
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
