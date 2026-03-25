package app.lifelinq.features.meals.api;

import app.lifelinq.config.RequestContext;
import app.lifelinq.features.meals.application.MealsApplicationService;
import app.lifelinq.features.meals.application.RecipeImportApplicationService;
import app.lifelinq.features.meals.contract.AddMealOutput;
import app.lifelinq.features.meals.contract.IngredientInput;
import app.lifelinq.features.meals.contract.RecipeDetailView;
import app.lifelinq.features.meals.contract.RecipeDraftView;
import app.lifelinq.features.meals.contract.RecipeDuplicateAssessmentView;
import app.lifelinq.features.meals.contract.IngredientView;
import app.lifelinq.features.meals.contract.PlannedMealView;
import app.lifelinq.features.meals.contract.RecentPlannedMealView;
import app.lifelinq.features.meals.contract.RecipeImportDraftIngredientView;
import app.lifelinq.features.meals.contract.RecipeImportDraftView;
import app.lifelinq.features.meals.contract.RecipeLibraryItemView;
import app.lifelinq.features.meals.contract.RecipeView;
import app.lifelinq.features.meals.contract.HouseholdPreferenceSummaryView;
import app.lifelinq.features.meals.contract.MealShoppingProjectionView;
import app.lifelinq.features.meals.contract.MealIdentitySummaryView;
import app.lifelinq.features.meals.contract.PlanningChoiceSupportView;
import app.lifelinq.features.meals.contract.RecentMealOccurrenceView;
import app.lifelinq.features.meals.contract.RecipeUsageSummaryView;
import app.lifelinq.features.meals.contract.WeekShoppingProjectionView;
import app.lifelinq.features.meals.contract.WeekPlanView;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MealsController {
    private final MealsApplicationService mealsApplicationService;
    private final RecipeImportApplicationService recipeImportApplicationService;

    public MealsController(
            MealsApplicationService mealsApplicationService,
            RecipeImportApplicationService recipeImportApplicationService
    ) {
        this.mealsApplicationService = mealsApplicationService;
        this.recipeImportApplicationService = recipeImportApplicationService;
    }

    @PostMapping("/meals/recipes")
    public ResponseEntity<?> createRecipe(@RequestBody CreateOrUpdateRecipeRequest request) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        RecipeView recipe = mealsApplicationService.createRecipe(
                context.getGroupId(),
                context.getUserId(),
                request.getName(),
                request.getSourceName(),
                request.getSourceUrl(),
                request.getOriginKind(),
                request.getServings(),
                request.getShortNote(),
                request.getInstructions(),
                request.getSavedInRecipes(),
                toIngredientInputs(request.getIngredients())
        );
        return ResponseEntity.ok(toRecipeResponse(recipe));
    }

    @PostMapping("/meals/recipe-drafts/manual")
    public ResponseEntity<?> createManualRecipeDraft() {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        RecipeDraftView draft = mealsApplicationService.createManualRecipeDraft(
                context.getGroupId(),
                context.getUserId()
        );
        return ResponseEntity.ok(draft);
    }

    @PostMapping("/meals/recipe-drafts/from-url")
    public ResponseEntity<?> createRecipeDraftFromUrl(@RequestBody CreateRecipeDraftFromUrlRequest request) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        RecipeDraftView draft = mealsApplicationService.createRecipeDraftFromUrl(
                context.getGroupId(),
                context.getUserId(),
                request.getUrl()
        );
        return ResponseEntity.ok(draft);
    }

    @GetMapping("/meals/recipe-drafts/{draftId}")
    public ResponseEntity<?> getRecipeDraft(@PathVariable UUID draftId) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        RecipeDraftView draft = mealsApplicationService.getRecipeDraft(
                context.getGroupId(),
                context.getUserId(),
                draftId
        );
        return ResponseEntity.ok(draft);
    }

    @PutMapping("/meals/recipe-drafts/{draftId}")
    public ResponseEntity<?> updateRecipeDraft(
            @PathVariable UUID draftId,
            @RequestBody UpdateRecipeDraftRequest request
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        RecipeDraftView draft = mealsApplicationService.updateRecipeDraft(
                context.getGroupId(),
                context.getUserId(),
                draftId,
                request.getName(),
                request.getSourceName(),
                request.getSourceUrl(),
                request.getServings(),
                request.getShortNote(),
                request.getInstructions(),
                request.getMarkReady(),
                toIngredientInputs(request.getIngredients())
        );
        return ResponseEntity.ok(draft);
    }

    @GetMapping("/meals/recipe-drafts/{draftId}/duplicate-assessment")
    public ResponseEntity<?> getRecipeDraftDuplicateAssessment(@PathVariable UUID draftId) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        RecipeDuplicateAssessmentView assessment = mealsApplicationService.getRecipeDraftDuplicateAssessment(
                context.getGroupId(),
                context.getUserId(),
                draftId
        );
        return ResponseEntity.ok(assessment);
    }

    @PostMapping("/meals/recipe-drafts/{draftId}/accept")
    public ResponseEntity<?> acceptRecipeDraft(
            @PathVariable UUID draftId,
            @RequestBody(required = false) AcceptRecipeDraftRequest request
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        RecipeDetailView recipe = mealsApplicationService.acceptRecipeDraft(
                context.getGroupId(),
                context.getUserId(),
                draftId,
                request != null && Boolean.TRUE.equals(request.getAllowDuplicate())
        );
        return ResponseEntity.ok(recipe);
    }

    @GetMapping("/meals/recipe-library/items")
    public ResponseEntity<?> listRecipeLibraryItems(
            @RequestParam(name = "state", required = false, defaultValue = "active") String state
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        List<RecipeLibraryItemView> items = mealsApplicationService.listRecipeLibraryItems(
                context.getGroupId(),
                context.getUserId(),
                state
        );
        return ResponseEntity.ok(items);
    }

    @GetMapping("/meals/recipe-library/recent-items")
    public ResponseEntity<?> listRecentRecipeLibraryItems() {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        List<RecipeLibraryItemView> items = mealsApplicationService.listRecentlyUsedRecipeLibraryItems(
                context.getGroupId(),
                context.getUserId()
        );
        return ResponseEntity.ok(items);
    }

    @GetMapping("/meals/recipe-details/{recipeId}")
    public ResponseEntity<?> getRecipeDetail(@PathVariable UUID recipeId) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        RecipeDetailView recipe = mealsApplicationService.getRecipeDetail(
                context.getGroupId(),
                context.getUserId(),
                recipeId
        );
        return ResponseEntity.ok(recipe);
    }

    @PutMapping("/meals/recipe-details/{recipeId}")
    public ResponseEntity<?> updateRecipeDetail(
            @PathVariable UUID recipeId,
            @RequestBody CreateOrUpdateRecipeRequest request
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        mealsApplicationService.updateRecipe(
                context.getGroupId(),
                context.getUserId(),
                recipeId,
                request.getName(),
                request.getSourceName(),
                request.getSourceUrl(),
                request.getOriginKind(),
                request.getServings(),
                request.getShortNote(),
                request.getInstructions(),
                request.getSavedInRecipes(),
                toIngredientInputs(request.getIngredients())
        );
        RecipeDetailView recipe = mealsApplicationService.getRecipeDetail(
                context.getGroupId(),
                context.getUserId(),
                recipeId
        );
        return ResponseEntity.ok(recipe);
    }

    @PostMapping("/meals/recipe-details/{recipeId}/archive")
    public ResponseEntity<?> archiveRecipeDetail(@PathVariable UUID recipeId) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        mealsApplicationService.archiveRecipe(
                context.getGroupId(),
                context.getUserId(),
                recipeId
        );
        RecipeDetailView recipe = mealsApplicationService.getRecipeDetail(
                context.getGroupId(),
                context.getUserId(),
                recipeId
        );
        return ResponseEntity.ok(recipe);
    }

    @PostMapping("/meals/recipe-details/{recipeId}/restore")
    public ResponseEntity<?> restoreRecipeDetail(@PathVariable UUID recipeId) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        mealsApplicationService.restoreRecipe(
                context.getGroupId(),
                context.getUserId(),
                recipeId
        );
        RecipeDetailView recipe = mealsApplicationService.getRecipeDetail(
                context.getGroupId(),
                context.getUserId(),
                recipeId
        );
        return ResponseEntity.ok(recipe);
    }

    @PostMapping("/meals/recipe-details/{recipeId}/make-soon")
    public ResponseEntity<?> markRecipeDetailMakeSoon(@PathVariable UUID recipeId) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        mealsApplicationService.markRecipeMakeSoon(
                context.getGroupId(),
                context.getUserId(),
                recipeId
        );
        RecipeDetailView recipe = mealsApplicationService.getRecipeDetail(
                context.getGroupId(),
                context.getUserId(),
                recipeId
        );
        return ResponseEntity.ok(recipe);
    }

    @DeleteMapping("/meals/recipe-details/{recipeId}/make-soon")
    public ResponseEntity<?> clearRecipeDetailMakeSoon(@PathVariable UUID recipeId) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        mealsApplicationService.clearRecipeMakeSoon(
                context.getGroupId(),
                context.getUserId(),
                recipeId
        );
        RecipeDetailView recipe = mealsApplicationService.getRecipeDetail(
                context.getGroupId(),
                context.getUserId(),
                recipeId
        );
        return ResponseEntity.ok(recipe);
    }

    @GetMapping("/meals/recipes")
    public ResponseEntity<?> listRecipes() {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        List<RecipeResponse> recipes = new ArrayList<>();
        for (RecipeView recipe : mealsApplicationService.listRecipes(context.getGroupId(), context.getUserId())) {
            recipes.add(toRecipeResponse(recipe));
        }
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/meals/recipes/archived")
    public ResponseEntity<?> listArchivedRecipes() {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        List<RecipeResponse> recipes = new ArrayList<>();
        for (RecipeView recipe : mealsApplicationService.listArchivedRecipes(context.getGroupId(), context.getUserId())) {
            recipes.add(toRecipeResponse(recipe));
        }
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/meals/recipes/recently-used")
    public ResponseEntity<?> listRecentlyUsedRecipes() {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        List<RecipeResponse> recipes = new ArrayList<>();
        for (RecipeView recipe : mealsApplicationService.listRecentlyUsedRecipes(context.getGroupId(), context.getUserId())) {
            recipes.add(toRecipeResponse(recipe));
        }
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/meals/recently-planned")
    public ResponseEntity<?> listRecentPlannedMeals() {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        List<RecentPlannedMealResponse> meals = new ArrayList<>();
        for (RecentPlannedMealView meal : mealsApplicationService.listRecentPlannedMeals(context.getGroupId(), context.getUserId())) {
            meals.add(new RecentPlannedMealResponse(
                    meal.year(),
                    meal.isoWeek(),
                    meal.dayOfWeek(),
                    meal.mealType(),
                    meal.mealTitle(),
                    meal.recipeId(),
                    meal.recipeTitle()
            ));
        }
        return ResponseEntity.ok(meals);
    }

    @GetMapping("/meals/household-memory/recent-occurrences")
    public ResponseEntity<?> listRecentMealOccurrences(
            @RequestParam(name = "limit", required = false, defaultValue = "12") int limit
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        List<RecentMealOccurrenceView> views = mealsApplicationService.listRecentMealOccurrences(
                context.getGroupId(),
                context.getUserId(),
                limit
        );
        return ResponseEntity.ok(views);
    }

    @GetMapping("/meals/household-memory/meal-identities")
    public ResponseEntity<?> listMealIdentitySummaries(
            @RequestParam(name = "limit", required = false, defaultValue = "12") int limit
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        List<MealIdentitySummaryView> views = mealsApplicationService.listMealIdentitySummaries(
                context.getGroupId(),
                context.getUserId(),
                limit
        );
        return ResponseEntity.ok(views);
    }

    @GetMapping("/meals/household-memory/recipe-usage")
    public ResponseEntity<?> listRecipeUsageSummaries(
            @RequestParam(name = "limit", required = false, defaultValue = "12") int limit
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        List<RecipeUsageSummaryView> views = mealsApplicationService.listRecipeUsageSummaries(
                context.getGroupId(),
                context.getUserId(),
                limit
        );
        return ResponseEntity.ok(views);
    }

    @GetMapping("/meals/choice-support/recipes/{recipeId}/memory")
    public ResponseEntity<?> getRecipeUsageSummary(@PathVariable UUID recipeId) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        RecipeUsageSummaryView view = mealsApplicationService.getRecipeUsageSummary(
                context.getGroupId(),
                context.getUserId(),
                recipeId
        );
        return ResponseEntity.ok(view);
    }

    @GetMapping("/meals/household-memory/preferences")
    public ResponseEntity<?> listHouseholdPreferenceSummaries() {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        List<HouseholdPreferenceSummaryView> views = mealsApplicationService.listHouseholdPreferenceSummaries(
                context.getGroupId(),
                context.getUserId()
        );
        return ResponseEntity.ok(views);
    }

    @PostMapping("/meals/household-memory/preferences")
    public ResponseEntity<?> writeHouseholdPreferenceSignal(@RequestBody WriteHouseholdPreferenceSignalRequest request) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        HouseholdPreferenceSummaryView view = mealsApplicationService.writeHouseholdPreferenceSignal(
                context.getGroupId(),
                context.getUserId(),
                request.getTargetKind(),
                request.getSignalType(),
                request.getRecipeId(),
                request.getMealIdentityKey()
        );
        return ResponseEntity.ok(view);
    }

    @PostMapping("/meals/household-memory/preferences/clear")
    public ResponseEntity<?> clearHouseholdPreferenceSignal(@RequestBody WriteHouseholdPreferenceSignalRequest request) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        mealsApplicationService.clearHouseholdPreferenceSignal(
                context.getGroupId(),
                context.getUserId(),
                request.getTargetKind(),
                request.getSignalType(),
                request.getRecipeId(),
                request.getMealIdentityKey()
        );
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/meals/choice-support/slot")
    public ResponseEntity<?> getSlotPlanningChoiceSupport(
            @RequestParam int year,
            @RequestParam int isoWeek,
            @RequestParam int dayOfWeek,
            @RequestParam String mealType
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        PlanningChoiceSupportView view = mealsApplicationService.getSlotPlanningChoiceSupport(
                context.getGroupId(),
                context.getUserId(),
                year,
                isoWeek,
                dayOfWeek,
                app.lifelinq.features.meals.domain.MealType.valueOf(mealType)
        );
        return ResponseEntity.ok(view);
    }

    @GetMapping("/meals/choice-support/tonight")
    public ResponseEntity<?> getTonightPlanningChoiceSupport() {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        PlanningChoiceSupportView view = mealsApplicationService.getTonightPlanningChoiceSupport(
                context.getGroupId(),
                context.getUserId()
        );
        return ResponseEntity.ok(view);
    }

    @GetMapping("/meals/choice-support/week-start")
    public ResponseEntity<?> getWeekStartPlanningChoiceSupport(
            @RequestParam int year,
            @RequestParam int isoWeek
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        PlanningChoiceSupportView view = mealsApplicationService.getWeekStartPlanningChoiceSupport(
                context.getGroupId(),
                context.getUserId(),
                year,
                isoWeek
        );
        return ResponseEntity.ok(view);
    }

    @GetMapping("/meals/recipes/{recipeId}")
    public ResponseEntity<?> getRecipe(@PathVariable UUID recipeId) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        RecipeView recipe = mealsApplicationService.getRecipe(context.getGroupId(), context.getUserId(), recipeId);
        return ResponseEntity.ok(toRecipeResponse(recipe));
    }

    @PutMapping("/meals/recipes/{recipeId}")
    public ResponseEntity<?> updateRecipe(
            @PathVariable UUID recipeId,
            @RequestBody CreateOrUpdateRecipeRequest request
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        RecipeView recipe = mealsApplicationService.updateRecipe(
                context.getGroupId(),
                context.getUserId(),
                recipeId,
                request.getName(),
                request.getSourceName(),
                request.getSourceUrl(),
                request.getOriginKind(),
                request.getServings(),
                request.getShortNote(),
                request.getInstructions(),
                request.getSavedInRecipes(),
                toIngredientInputs(request.getIngredients())
        );
        return ResponseEntity.ok(toRecipeResponse(recipe));
    }

    @PostMapping("/meals/recipes/{recipeId}/archive")
    public ResponseEntity<?> archiveRecipe(@PathVariable UUID recipeId) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        RecipeView recipe = mealsApplicationService.archiveRecipe(
                context.getGroupId(),
                context.getUserId(),
                recipeId
        );
        return ResponseEntity.ok(toRecipeResponse(recipe));
    }

    @PostMapping("/meals/recipes/{recipeId}/make-soon")
    public ResponseEntity<?> markRecipeMakeSoon(@PathVariable UUID recipeId) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        RecipeView recipe = mealsApplicationService.markRecipeMakeSoon(
                context.getGroupId(),
                context.getUserId(),
                recipeId
        );
        return ResponseEntity.ok(toRecipeResponse(recipe));
    }

    @DeleteMapping("/meals/recipes/{recipeId}/make-soon")
    public ResponseEntity<?> clearRecipeMakeSoon(@PathVariable UUID recipeId) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        RecipeView recipe = mealsApplicationService.clearRecipeMakeSoon(
                context.getGroupId(),
                context.getUserId(),
                recipeId
        );
        return ResponseEntity.ok(toRecipeResponse(recipe));
    }

    @PostMapping("/meals/recipes/{recipeId}/restore")
    public ResponseEntity<?> restoreRecipe(@PathVariable UUID recipeId) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        RecipeView recipe = mealsApplicationService.restoreRecipe(
                context.getGroupId(),
                context.getUserId(),
                recipeId
        );
        return ResponseEntity.ok(toRecipeResponse(recipe));
    }

    @DeleteMapping("/meals/recipes/{recipeId}")
    public ResponseEntity<?> deleteRecipe(@PathVariable UUID recipeId) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        mealsApplicationService.deleteRecipe(
                context.getGroupId(),
                context.getUserId(),
                recipeId
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/meals/recipes/import-drafts")
    public ResponseEntity<?> createRecipeImportDraft(@RequestBody CreateRecipeImportDraftRequest request) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        RecipeImportDraftView draft = recipeImportApplicationService.importRecipeDraft(
                context.getGroupId(),
                context.getUserId(),
                request.getUrl()
        );
        return ResponseEntity.ok(toRecipeImportDraftResponse(draft));
    }

    @PostMapping({
            "/meals/weeks/{year}/{isoWeek}/days/{dayOfWeek}",
            "/meals/weeks/{year}/{isoWeek}/days/{dayOfWeek}/meals/{mealType}"
    })
    public ResponseEntity<?> addOrReplaceMeal(
            @PathVariable int year,
            @PathVariable int isoWeek,
            @PathVariable int dayOfWeek,
            @PathVariable(required = false) String mealType,
            @RequestBody AddMealRequest request
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null) {
            return ApiScoping.missingContext();
        }
        if (context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        String resolvedMealType = mealType != null ? mealType : request.getMealType();
        if (resolvedMealType == null) {
            throw new IllegalArgumentException("mealType must not be null");
        }
        AddMealOutput output = mealsApplicationService.addOrReplaceMeal(
                context.getGroupId(),
                context.getUserId(),
                year,
                isoWeek,
                dayOfWeek,
                app.lifelinq.features.meals.domain.MealType.valueOf(resolvedMealType),
                request.getMealTitle(),
                request.getRecipeId(),
                request.getTargetShoppingListId(),
                request.getSelectedIngredientPositions()
        );
        return ResponseEntity.ok(toAddMealResponse(output));
    }

    @DeleteMapping("/meals/weeks/{year}/{isoWeek}/days/{dayOfWeek}/meals/{mealType}")
    public ResponseEntity<?> removeMeal(
            @PathVariable int year,
            @PathVariable int isoWeek,
            @PathVariable int dayOfWeek,
            @PathVariable String mealType
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null) {
            return ApiScoping.missingContext();
        }
        if (context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        mealsApplicationService.removeMeal(
                context.getGroupId(),
                context.getUserId(),
                year,
                isoWeek,
                dayOfWeek,
                app.lifelinq.features.meals.domain.MealType.valueOf(mealType)
        );
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/meals/weeks/{year}/{isoWeek}")
    public ResponseEntity<?> getWeekPlan(
            @PathVariable int year,
            @PathVariable int isoWeek
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null) {
            return ApiScoping.missingContext();
        }
        if (context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        WeekPlanView view = mealsApplicationService.getWeekPlan(
                context.getGroupId(),
                context.getUserId(),
                year,
                isoWeek
        );
        return ResponseEntity.ok(toWeekPlanResponse(view));
    }

    @GetMapping("/meals/weeks/{year}/{isoWeek}/shopping-impact")
    public ResponseEntity<?> getWeekShoppingProjection(
            @PathVariable int year,
            @PathVariable int isoWeek
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        WeekShoppingProjectionView view = mealsApplicationService.getWeekShoppingProjection(
                context.getGroupId(),
                context.getUserId(),
                year,
                isoWeek
        );
        return ResponseEntity.ok(view);
    }

    @GetMapping("/meals/weeks/{year}/{isoWeek}/days/{dayOfWeek}/meals/{mealType}/shopping-impact")
    public ResponseEntity<?> getMealShoppingProjection(
            @PathVariable int year,
            @PathVariable int isoWeek,
            @PathVariable int dayOfWeek,
            @PathVariable String mealType,
            @RequestParam(required = false) UUID shoppingListId
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        MealShoppingProjectionView view = mealsApplicationService.getMealShoppingProjection(
                context.getGroupId(),
                context.getUserId(),
                year,
                isoWeek,
                dayOfWeek,
                app.lifelinq.features.meals.domain.MealType.valueOf(mealType),
                shoppingListId
        );
        return ResponseEntity.ok(view);
    }

    private AddMealResponse toAddMealResponse(AddMealOutput output) {
        PlannedMealView meal = output.meal();
        return new AddMealResponse(
                output.weekPlanId(),
                output.year(),
                output.isoWeek(),
                new PlannedMealResponse(
                        meal.dayOfWeek(),
                        meal.mealType(),
                        meal.recipeId(),
                        meal.mealTitle(),
                        meal.recipeTitle(),
                        meal.shoppingHandledAt(),
                        meal.shoppingListId()
                )
        );
    }

    private WeekPlanResponse toWeekPlanResponse(WeekPlanView view) {
        List<PlannedMealResponse> meals = new ArrayList<>();
        for (PlannedMealView meal : view.meals()) {
            meals.add(new PlannedMealResponse(
                    meal.dayOfWeek(),
                    meal.mealType(),
                    meal.recipeId(),
                    meal.mealTitle(),
                    meal.recipeTitle(),
                    meal.shoppingHandledAt(),
                    meal.shoppingListId()
            ));
        }
        return new WeekPlanResponse(
                view.weekPlanId(),
                view.year(),
                view.isoWeek(),
                view.createdAt(),
                meals
        );
    }

    private RecipeResponse toRecipeResponse(RecipeView view) {
        List<IngredientResponse> ingredients = new ArrayList<>();
        for (IngredientView ingredient : view.ingredients()) {
            ingredients.add(new IngredientResponse(
                    ingredient.id(),
                    ingredient.name(),
                    ingredient.rawText(),
                    ingredient.quantity(),
                    ingredient.unit(),
                    ingredient.position()
            ));
        }
        return new RecipeResponse(
                view.recipeId(),
                view.groupId(),
                view.name(),
                view.sourceName(),
                view.sourceUrl(),
                view.originKind(),
                view.servings(),
                view.makeSoonAt(),
                view.shortNote(),
                view.instructions(),
                view.createdAt(),
                view.updatedAt(),
                view.archivedAt(),
                view.savedInRecipes(),
                view.deleteEligible(),
                view.deleteBlockedReason(),
                ingredients
        );
    }

    private List<IngredientInput> toIngredientInputs(List<IngredientRequest> requests) {
        if (requests == null) {
            return List.of();
        }
        List<IngredientInput> inputs = new ArrayList<>();
        for (IngredientRequest request : requests) {
            inputs.add(new IngredientInput(
                    request.getName(),
                    request.getRawText(),
                    request.getQuantity(),
                    request.getUnit(),
                    request.getPosition()
            ));
        }
        return inputs;
    }

    private RecipeImportDraftResponse toRecipeImportDraftResponse(RecipeImportDraftView view) {
        List<RecipeImportDraftIngredientResponse> ingredients = new ArrayList<>();
        for (RecipeImportDraftIngredientView ingredient : view.ingredients()) {
            ingredients.add(new RecipeImportDraftIngredientResponse(
                    ingredient.name(),
                    ingredient.rawText(),
                    ingredient.quantity(),
                    ingredient.unit(),
                    ingredient.position()
            ));
        }
        return new RecipeImportDraftResponse(
                view.name(),
                view.sourceName(),
                view.sourceUrl(),
                view.originKind(),
                view.servings(),
                view.shortNote(),
                view.instructions(),
                ingredients
        );
    }
}
