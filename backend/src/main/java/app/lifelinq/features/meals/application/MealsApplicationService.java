package app.lifelinq.features.meals.application;

import app.lifelinq.features.group.contract.AccessDeniedException;
import app.lifelinq.features.group.contract.EnsureGroupMemberUseCase;
import app.lifelinq.features.meals.contract.AddMealOutput;
import app.lifelinq.features.meals.contract.AggregatedIngredientComparisonView;
import app.lifelinq.features.meals.contract.AggregatedIngredientNeedView;
import app.lifelinq.features.meals.contract.ContributorMealReferenceView;
import app.lifelinq.features.meals.contract.IngredientInput;
import app.lifelinq.features.meals.contract.RecipeAssetIntakePort;
import app.lifelinq.features.meals.contract.RecipeAssetIntakeReference;
import app.lifelinq.features.meals.contract.RecipeDocumentAssetStore;
import app.lifelinq.features.meals.contract.IngredientUnitView;
import app.lifelinq.features.meals.contract.IngredientView;
import app.lifelinq.features.meals.contract.HouseholdPreferenceSummaryView;
import app.lifelinq.features.meals.contract.IngredientCoverageView;
import app.lifelinq.features.meals.contract.MealChoiceCandidateView;
import app.lifelinq.features.meals.contract.MealIngredientNeedView;
import app.lifelinq.features.meals.contract.MealReadinessView;
import app.lifelinq.features.meals.contract.MealShoppingProjectionView;
import app.lifelinq.features.meals.contract.MealIdentitySummaryView;
import app.lifelinq.features.meals.contract.MealsShoppingPort;
import app.lifelinq.features.meals.contract.MealsShoppingListSnapshot;
import app.lifelinq.features.meals.contract.MealsShoppingListNotFoundException;
import app.lifelinq.features.meals.contract.PlannedMealView;
import app.lifelinq.features.meals.contract.PlanningChoiceSupportView;
import app.lifelinq.features.meals.contract.RecentPlannedMealView;
import app.lifelinq.features.meals.contract.RecentMealOccurrenceView;
import app.lifelinq.features.meals.contract.RecipeDetailView;
import app.lifelinq.features.meals.contract.RecipeDraftView;
import app.lifelinq.features.meals.contract.RecipeDuplicateAssessmentView;
import app.lifelinq.features.meals.contract.RecipeIdentitySummaryView;
import app.lifelinq.features.meals.contract.RecipeImportPort;
import app.lifelinq.features.meals.contract.RecipeLibraryItemView;
import app.lifelinq.features.meals.contract.RecipeLifecycleView;
import app.lifelinq.features.meals.contract.RecipeProvenanceView;
import app.lifelinq.features.meals.contract.RecipeSourceView;
import app.lifelinq.features.meals.contract.RecipeUsageSummaryView;
import app.lifelinq.features.meals.contract.RecipeView;
import app.lifelinq.features.meals.contract.ShoppingDeltaView;
import app.lifelinq.features.meals.contract.ShoppingLinkReferenceView;
import app.lifelinq.features.meals.contract.WeekShoppingReviewLinkView;
import app.lifelinq.features.meals.contract.WeekShoppingReviewView;
import app.lifelinq.features.meals.contract.WeekShoppingProjectionView;
import app.lifelinq.features.meals.contract.WeekPlanView;
import app.lifelinq.features.meals.domain.AggregatedIngredientComparison;
import app.lifelinq.features.meals.domain.AggregatedIngredientComparisonState;
import app.lifelinq.features.meals.domain.AggregatedIngredientNeed;
import app.lifelinq.features.meals.domain.HouseholdPreferenceSignal;
import app.lifelinq.features.meals.domain.HouseholdPreferenceSignalRepository;
import app.lifelinq.features.meals.domain.HouseholdPreferenceSignalTargetKind;
import app.lifelinq.features.meals.domain.HouseholdPreferenceSignalType;
import app.lifelinq.features.meals.domain.Ingredient;
import app.lifelinq.features.meals.domain.IngredientCoverage;
import app.lifelinq.features.meals.domain.IngredientCoverageState;
import app.lifelinq.features.meals.domain.IngredientUnit;
import app.lifelinq.features.meals.domain.KitchenReadinessEngine;
import app.lifelinq.features.meals.domain.MealChoiceSupportEngine;
import app.lifelinq.features.meals.domain.MealIdentity;
import app.lifelinq.features.meals.domain.MealIdentityKind;
import app.lifelinq.features.meals.domain.MealIngredientNeed;
import app.lifelinq.features.meals.domain.MealMemoryRepository;
import app.lifelinq.features.meals.domain.MealOccurrence;
import app.lifelinq.features.meals.domain.MealReadinessSignal;
import app.lifelinq.features.meals.domain.MealReadinessState;
import app.lifelinq.features.meals.domain.MealShoppingProjection;
import app.lifelinq.features.meals.domain.MealType;
import app.lifelinq.features.meals.domain.MealUsageAggregate;
import app.lifelinq.features.meals.domain.PlanningChoiceSupport;
import app.lifelinq.features.meals.domain.PlanningContext;
import app.lifelinq.features.meals.domain.PlanningScenario;
import app.lifelinq.features.meals.domain.PlannedMeal;
import app.lifelinq.features.meals.domain.RecentPlannedMeal;
import app.lifelinq.features.meals.domain.Recipe;
import app.lifelinq.features.meals.domain.RecipeDraft;
import app.lifelinq.features.meals.domain.RecipeDraftRepository;
import app.lifelinq.features.meals.domain.RecipeDraftState;
import app.lifelinq.features.meals.domain.RecipeDuplicateAssessment;
import app.lifelinq.features.meals.domain.RecipeDuplicateMatchType;
import app.lifelinq.features.meals.domain.RecipeInstructions;
import app.lifelinq.features.meals.domain.RecipeLifecycle;
import app.lifelinq.features.meals.domain.RecipeOriginKind;
import app.lifelinq.features.meals.domain.RecipeProvenance;
import app.lifelinq.features.meals.domain.RecipeRepository;
import app.lifelinq.features.meals.domain.RecipeSource;
import app.lifelinq.features.meals.domain.RecipeUsageHistory;
import app.lifelinq.features.meals.domain.ReuseCandidate;
import app.lifelinq.features.meals.domain.ReuseCandidateFamily;
import app.lifelinq.features.meals.domain.ShoppingCoverageState;
import app.lifelinq.features.meals.domain.ShoppingDelta;
import app.lifelinq.features.meals.domain.ShoppingLinkReference;
import app.lifelinq.features.meals.domain.ShoppingLinkStatus;
import app.lifelinq.features.meals.domain.WeekPlan;
import app.lifelinq.features.meals.domain.WeekShoppingProjection;
import app.lifelinq.features.meals.domain.WeekShoppingReview;
import app.lifelinq.features.meals.domain.WeekShoppingReviewEngine;
import app.lifelinq.features.meals.domain.WeekShoppingReviewLink;
import app.lifelinq.features.meals.domain.WeekPlanRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.transaction.annotation.Transactional;

public class MealsApplicationService {
    private static final int RECENTLY_USED_RECIPES_LIMIT = 3;
    private static final int RECENTLY_USED_MEALS_LIMIT = 6;
    private static final List<String> LEADING_INGREDIENT_MODIFIERS = List.of(
            "very finely chopped",
            "finely chopped",
            "roughly chopped",
            "lightly beaten",
            "chopped",
            "crushed",
            "mashed",
            "large",
            "small",
            "medium",
            "finhackad",
            "grovhackad",
            "hackad"
    );
    private static final List<String> LEADING_COOKING_MEASURE_TOKENS = List.of(
            "tbsp.", "tbsp",
            "tsp.", "tsp",
            "msk",
            "tsk",
            "cloves", "clove",
            "slices", "slice",
            "pinches", "pinch"
    );
    private static final List<String> TRAILING_COUNT_TOKENS = List.of(
            "cloves", "clove",
            "slices", "slice"
    );
    private static final List<String> SHOPPING_CONTEXT_SUFFIXES = List.of(
            "plus a little extra",
            "plus extra",
            "to serve",
            "for serving",
            "for frying",
            "for dusting",
            "for greasing",
            "till servering",
            "till stekning"
    );
    private static final List<String> SHOPPING_PREPARATION_SUFFIXES = List.of(
            "very finely chopped",
            "finely chopped",
            "roughly chopped",
            "lightly beaten",
            "beaten",
            "crushed",
            "chopped",
            "finhackad",
            "grovhackad",
            "hackad"
    );
    private static final Map<String, String> SHOPPING_CANONICAL_NAME_ENDINGS = Map.of(
            "thyme leaves", "thyme"
    );
    private static final Pattern LEADING_QUANTITY_PATTERN = Pattern.compile(
            "^(?:(?:\\d+[.,]?\\d*|\\d+\\s+\\d+/\\d+|\\d+/\\d+|[½¼¾⅓⅔])\\s+)+"
    );

    private final WeekPlanRepository weekPlanRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeDraftRepository recipeDraftRepository;
    private final MealMemoryRepository mealMemoryRepository;
    private final HouseholdPreferenceSignalRepository householdPreferenceSignalRepository;
    private final RecipeImportPort recipeImportPort;
    private final RecipeAssetIntakePort recipeAssetIntakePort;
    private final RecipeDocumentAssetStore recipeDocumentAssetStore;
    private final EnsureGroupMemberUseCase ensureGroupMemberUseCase;
    private final MealsShoppingPort mealsShoppingPort;
    private final Clock clock;
    private final MealChoiceSupportEngine mealChoiceSupportEngine;
    private final KitchenReadinessEngine kitchenReadinessEngine;
    private final WeekShoppingReviewEngine weekShoppingReviewEngine;

    public MealsApplicationService(
            WeekPlanRepository weekPlanRepository,
            RecipeRepository recipeRepository,
            EnsureGroupMemberUseCase ensureGroupMemberUseCase,
            MealsShoppingPort mealsShoppingPort,
            Clock clock
    ) {
        this(
                weekPlanRepository,
                recipeRepository,
                null,
                null,
                null,
                null,
                null,
                null,
                ensureGroupMemberUseCase,
                mealsShoppingPort,
                clock
        );
    }

    public MealsApplicationService(
            WeekPlanRepository weekPlanRepository,
            RecipeRepository recipeRepository,
            RecipeDraftRepository recipeDraftRepository,
            MealMemoryRepository mealMemoryRepository,
            HouseholdPreferenceSignalRepository householdPreferenceSignalRepository,
            RecipeImportPort recipeImportPort,
            EnsureGroupMemberUseCase ensureGroupMemberUseCase,
            MealsShoppingPort mealsShoppingPort,
            Clock clock
    ) {
        this(
                weekPlanRepository,
                recipeRepository,
                recipeDraftRepository,
                mealMemoryRepository,
                householdPreferenceSignalRepository,
                recipeImportPort,
                null,
                null,
                ensureGroupMemberUseCase,
                mealsShoppingPort,
                clock
        );
    }

    public MealsApplicationService(
            WeekPlanRepository weekPlanRepository,
            RecipeRepository recipeRepository,
            RecipeDraftRepository recipeDraftRepository,
            MealMemoryRepository mealMemoryRepository,
            HouseholdPreferenceSignalRepository householdPreferenceSignalRepository,
            RecipeImportPort recipeImportPort,
            RecipeAssetIntakePort recipeAssetIntakePort,
            RecipeDocumentAssetStore recipeDocumentAssetStore,
            EnsureGroupMemberUseCase ensureGroupMemberUseCase,
            MealsShoppingPort mealsShoppingPort,
            Clock clock
    ) {
        if (weekPlanRepository == null) {
            throw new IllegalArgumentException("weekPlanRepository must not be null");
        }
        if (recipeRepository == null) {
            throw new IllegalArgumentException("recipeRepository must not be null");
        }
        if (ensureGroupMemberUseCase == null) {
            throw new IllegalArgumentException("ensureGroupMemberUseCase must not be null");
        }
        if (mealsShoppingPort == null) {
            throw new IllegalArgumentException("mealsShoppingPort must not be null");
        }
        if (clock == null) {
            throw new IllegalArgumentException("clock must not be null");
        }
        this.weekPlanRepository = weekPlanRepository;
        this.recipeRepository = recipeRepository;
        this.recipeDraftRepository = recipeDraftRepository;
        this.mealMemoryRepository = mealMemoryRepository;
        this.householdPreferenceSignalRepository = householdPreferenceSignalRepository;
        this.recipeImportPort = recipeImportPort;
        this.recipeAssetIntakePort = recipeAssetIntakePort;
        this.recipeDocumentAssetStore = recipeDocumentAssetStore;
        this.ensureGroupMemberUseCase = ensureGroupMemberUseCase;
        this.mealsShoppingPort = mealsShoppingPort;
        this.clock = clock;
        this.mealChoiceSupportEngine = new MealChoiceSupportEngine();
        this.kitchenReadinessEngine = new KitchenReadinessEngine();
        this.weekShoppingReviewEngine = new WeekShoppingReviewEngine();
    }

    public MealsApplicationService(
            WeekPlanRepository weekPlanRepository,
            RecipeRepository recipeRepository,
            RecipeDraftRepository recipeDraftRepository,
            RecipeImportPort recipeImportPort,
            EnsureGroupMemberUseCase ensureGroupMemberUseCase,
            MealsShoppingPort mealsShoppingPort,
            Clock clock
    ) {
        this(
                weekPlanRepository,
                recipeRepository,
                recipeDraftRepository,
                null,
                null,
                recipeImportPort,
                null,
                null,
                ensureGroupMemberUseCase,
                mealsShoppingPort,
                clock
        );
    }

    public MealsApplicationService(
            WeekPlanRepository weekPlanRepository,
            RecipeRepository recipeRepository,
            RecipeDraftRepository recipeDraftRepository,
            RecipeImportPort recipeImportPort,
            RecipeAssetIntakePort recipeAssetIntakePort,
            RecipeDocumentAssetStore recipeDocumentAssetStore,
            EnsureGroupMemberUseCase ensureGroupMemberUseCase,
            MealsShoppingPort mealsShoppingPort,
            Clock clock
    ) {
        this(
                weekPlanRepository,
                recipeRepository,
                recipeDraftRepository,
                null,
                null,
                recipeImportPort,
                recipeAssetIntakePort,
                recipeDocumentAssetStore,
                ensureGroupMemberUseCase,
                mealsShoppingPort,
                clock
        );
    }

    @Transactional
    public RecipeView createRecipe(
            UUID groupId,
            UUID actorUserId,
            String name,
            String sourceName,
            String sourceUrl,
            String originKind,
            String servings,
            String shortNote,
            String instructions,
            Boolean savedInRecipes,
            List<IngredientInput> ingredients
    ) {
        ensureMealAccess(groupId, actorUserId);
        Instant now = clock.instant();
        Recipe recipe = new Recipe(
                UUID.randomUUID(),
                groupId,
                normalizeRecipeName(name),
                normalizeOptionalRecipeText(sourceName),
                normalizeOptionalRecipeUrl(sourceUrl),
                normalizeOriginKind(originKind),
                normalizeOptionalRecipeText(servings),
                null,
                normalizeOptionalRecipeText(shortNote),
                normalizeOptionalInstructions(instructions),
                now,
                now,
                null,
                savedInRecipes == null || savedInRecipes,
                toDomainIngredients(ingredients)
        );
        return toView(recipeRepository.save(recipe), false);
    }

    @Transactional
    public RecipeDraftView createManualRecipeDraft(UUID groupId, UUID actorUserId) {
        ensureMealAccess(groupId, actorUserId);
        requireRecipeDraftRepository();
        Instant now = clock.instant();
        RecipeDraft draft = new RecipeDraft(
                UUID.randomUUID(),
                groupId,
                null,
                new RecipeSource(null, null),
                new RecipeProvenance(RecipeOriginKind.MANUAL, null),
                null,
                null,
                new RecipeInstructions(null),
                RecipeDraftState.DRAFT_OPEN,
                now,
                now,
                List.of()
        );
        return toDraftView(recipeDraftRepository.save(draft));
    }

    @Transactional
    public RecipeDraftView createRecipeDraftFromUrl(UUID groupId, UUID actorUserId, String sourceUrl) {
        ensureMealAccess(groupId, actorUserId);
        requireRecipeDraftRepository();
        requireRecipeImportPort();
        Instant now = clock.instant();
        RecipeImportDraftSupport.RecipeDraftSeed seed = RecipeImportDraftSupport.importFromUrl(
                recipeImportPort,
                sourceUrl
        );
        RecipeDraft draft = new RecipeDraft(
                UUID.randomUUID(),
                groupId,
                seed.name(),
                seed.source(),
                seed.provenance(),
                seed.servings(),
                seed.shortNote(),
                seed.instructions(),
                seed.state(),
                now,
                now,
                seed.ingredients()
        );
        return toDraftView(recipeDraftRepository.save(draft));
    }

    @Transactional
    public RecipeDraftView createRecipeDraftFromText(UUID groupId, UUID actorUserId, String text) {
        ensureMealAccess(groupId, actorUserId);
        requireRecipeDraftRepository();
        Instant now = clock.instant();
        RecipeImportDraftSupport.RecipeDraftSeed seed = RecipeImportDraftSupport.importFromText(text);
        RecipeDraft draft = new RecipeDraft(
                UUID.randomUUID(),
                groupId,
                seed.name(),
                seed.source(),
                seed.provenance(),
                seed.servings(),
                seed.shortNote(),
                seed.instructions(),
                seed.state(),
                now,
                now,
                seed.ingredients()
        );
        return toDraftView(recipeDraftRepository.save(draft));
    }

    public RecipeAssetIntakeReference stageRecipeDocumentAsset(
            UUID groupId,
            UUID actorUserId,
            String sourceLabel,
            String originalFilename,
            String mimeType,
            byte[] content
    ) {
        ensureMealAccess(groupId, actorUserId);
        requireRecipeDocumentAssetStore();
        if (content == null || content.length == 0) {
            throw new RecipeImportFailedException(
                    "We could not use that file. Try another recipe PDF or document."
            );
        }
        return recipeDocumentAssetStore.stageDocument(
                sourceLabel,
                originalFilename,
                mimeType,
                content
        );
    }

    @Transactional
    public RecipeDraftView createRecipeDraftFromAsset(
            UUID groupId,
            UUID actorUserId,
            RecipeAssetIntakeReference reference
    ) {
        ensureMealAccess(groupId, actorUserId);
        requireRecipeDraftRepository();
        requireRecipeAssetIntakePort();
        Instant now = clock.instant();
        RecipeImportDraftSupport.RecipeDraftSeed seed = RecipeImportDraftSupport.importFromAsset(
                recipeAssetIntakePort,
                reference
        );
        RecipeDraft draft = new RecipeDraft(
                UUID.randomUUID(),
                groupId,
                seed.name(),
                seed.source(),
                seed.provenance(),
                seed.servings(),
                seed.shortNote(),
                seed.instructions(),
                seed.state(),
                now,
                now,
                seed.ingredients()
        );
        return toDraftView(recipeDraftRepository.save(draft));
    }

    @Transactional(readOnly = true)
    public RecipeDraftView getRecipeDraft(UUID groupId, UUID actorUserId, UUID draftId) {
        ensureMealAccess(groupId, actorUserId);
        return toDraftView(loadRecipeDraft(groupId, draftId));
    }

    @Transactional
    public RecipeDraftView updateRecipeDraft(
            UUID groupId,
            UUID actorUserId,
            UUID draftId,
            String name,
            String sourceName,
            String sourceUrl,
            String servings,
            String shortNote,
            String instructions,
            Boolean markReady,
            List<IngredientInput> ingredients
    ) {
        ensureMealAccess(groupId, actorUserId);
        RecipeDraft existing = loadRecipeDraft(groupId, draftId);
        Instant now = clock.instant();
        String resolvedName = name == null ? existing.getName() : normalizeOptionalRecipeText(name);
        RecipeSource resolvedSource = new RecipeSource(
                sourceName == null ? existing.getSource().sourceName() : normalizeOptionalRecipeText(sourceName),
                sourceUrl == null ? existing.getSource().sourceUrl() : normalizeOptionalRecipeUrl(sourceUrl)
        );
        List<Ingredient> resolvedIngredients = ingredients == null
                ? existing.getIngredients()
                : toDomainIngredients(ingredients);
        RecipeInstructions resolvedInstructions = instructions == null
                ? existing.getInstructions()
                : new RecipeInstructions(instructions);
        RecipeProvenance resolvedProvenance = new RecipeProvenance(
                existing.getProvenance().originKind(),
                resolvedSource.sourceUrl()
        );
        RecipeDraftState resolvedState = determineDraftState(
                resolvedProvenance,
                resolvedName,
                resolvedIngredients,
                Boolean.TRUE.equals(markReady)
        );
        RecipeDraft updated = new RecipeDraft(
                existing.getId(),
                existing.getGroupId(),
                resolvedName,
                resolvedSource,
                resolvedProvenance,
                servings == null ? existing.getServings() : normalizeOptionalRecipeText(servings),
                shortNote == null ? existing.getShortNote() : normalizeOptionalRecipeText(shortNote),
                resolvedInstructions,
                resolvedState,
                existing.getCreatedAt(),
                now,
                resolvedIngredients
        );
        return toDraftView(recipeDraftRepository.save(updated));
    }

    @Transactional(readOnly = true)
    public RecipeDuplicateAssessmentView getRecipeDraftDuplicateAssessment(
            UUID groupId,
            UUID actorUserId,
            UUID draftId
    ) {
        ensureMealAccess(groupId, actorUserId);
        RecipeDraft draft = loadRecipeDraft(groupId, draftId);
        return toDuplicateAssessmentView(groupId, assessDuplicateAttention(groupId, draft));
    }

    @Transactional
    public RecipeDetailView acceptRecipeDraft(
            UUID groupId,
            UUID actorUserId,
            UUID draftId,
            boolean allowDuplicate
    ) {
        ensureMealAccess(groupId, actorUserId);
        RecipeDraft draft = loadRecipeDraft(groupId, draftId);
        if (!hasDraftCoreContent(draft.getName(), draft.getIngredients())) {
            throw new IllegalArgumentException("Recipe draft is not ready to save.");
        }
        RecipeDuplicateAssessment duplicateAssessment = assessDuplicateAttention(groupId, draft);
        if (duplicateAssessment.attentionRequired() && !allowDuplicate) {
            throw new RecipeDuplicateAttentionRequiredException(duplicateAssessment.reason());
        }
        Instant now = clock.instant();
        Recipe savedRecipe = recipeRepository.save(new Recipe(
                UUID.randomUUID(),
                groupId,
                draft.getName(),
                draft.getSource().sourceName(),
                draft.getSource().sourceUrl(),
                draft.getProvenance().originKind(),
                draft.getServings(),
                null,
                draft.getShortNote(),
                draft.getInstructions().body(),
                now,
                now,
                null,
                true,
                draft.getIngredients()
        ));
        recipeDraftRepository.delete(draft);
        return toDetailView(savedRecipe);
    }

    @Transactional(readOnly = true)
    public List<RecipeLibraryItemView> listRecipeLibraryItems(UUID groupId, UUID actorUserId) {
        return listRecipeLibraryItems(groupId, actorUserId, "active");
    }

    @Transactional(readOnly = true)
    public List<RecipeLibraryItemView> listRecipeLibraryItems(UUID groupId, UUID actorUserId, String state) {
        ensureMealAccess(groupId, actorUserId);
        List<RecipeLibraryItemView> views = new ArrayList<>();
        for (Recipe recipe : findRecipesForLibraryState(groupId, state)) {
            views.add(toLibraryItemView(recipe));
        }
        views.sort((a, b) -> {
            int nameCompare = a.name().compareToIgnoreCase(b.name());
            if (nameCompare != 0) {
                return nameCompare;
            }
            return a.recipeId().compareTo(b.recipeId());
        });
        return views;
    }

    @Transactional(readOnly = true)
    public List<RecipeLibraryItemView> listRecentlyUsedRecipeLibraryItems(UUID groupId, UUID actorUserId) {
        ensureMealAccess(groupId, actorUserId);
        List<RecipeLibraryItemView> views = new ArrayList<>();
        for (Recipe recipe : findRecentlyUsedSavedRecipes(groupId)) {
            views.add(toLibraryItemView(recipe));
        }
        return views;
    }

    @Transactional(readOnly = true)
    public RecipeDetailView getRecipeDetail(UUID groupId, UUID actorUserId, UUID recipeId) {
        ensureMealAccess(groupId, actorUserId);
        return toDetailView(loadRecipe(groupId, recipeId));
    }

    @Transactional
    public RecipeView markRecipeMakeSoon(UUID groupId, UUID actorUserId, UUID recipeId) {
        ensureMealAccess(groupId, actorUserId);
        Recipe existing = loadRecipe(groupId, recipeId);
        Instant now = clock.instant();
        Recipe updated = new Recipe(
                existing.getId(),
                existing.getGroupId(),
                existing.getName(),
                existing.getSourceName(),
                existing.getSourceUrl(),
                existing.getOriginKind(),
                existing.getServings(),
                now,
                existing.getShortNote(),
                existing.getInstructions(),
                existing.getCreatedAt(),
                now,
                existing.getArchivedAt(),
                existing.isSavedInRecipes(),
                existing.getIngredients()
        );
        return toView(recipeRepository.save(updated), true);
    }

    @Transactional
    public RecipeView clearRecipeMakeSoon(UUID groupId, UUID actorUserId, UUID recipeId) {
        ensureMealAccess(groupId, actorUserId);
        Recipe existing = loadRecipe(groupId, recipeId);
        Instant now = clock.instant();
        Recipe updated = new Recipe(
                existing.getId(),
                existing.getGroupId(),
                existing.getName(),
                existing.getSourceName(),
                existing.getSourceUrl(),
                existing.getOriginKind(),
                existing.getServings(),
                null,
                existing.getShortNote(),
                existing.getInstructions(),
                existing.getCreatedAt(),
                now,
                existing.getArchivedAt(),
                existing.isSavedInRecipes(),
                existing.getIngredients()
        );
        return toView(recipeRepository.save(updated), true);
    }

    @Transactional(readOnly = true)
    public RecipeView getRecipe(UUID groupId, UUID actorUserId, UUID recipeId) {
        ensureMealAccess(groupId, actorUserId);
        return toView(loadRecipe(groupId, recipeId), true);
    }

    @Transactional(readOnly = true)
    public List<RecipeView> listRecipes(UUID groupId, UUID actorUserId) {
        ensureMealAccess(groupId, actorUserId);
        List<RecipeView> views = new ArrayList<>();
        for (Recipe recipe : recipeRepository.findActiveByGroupId(groupId)) {
            views.add(toView(recipe, false));
        }
        views.sort((a, b) -> {
            int nameCompare = a.name().compareToIgnoreCase(b.name());
            if (nameCompare != 0) {
                return nameCompare;
            }
            return a.recipeId().compareTo(b.recipeId());
        });
        return views;
    }

    @Transactional(readOnly = true)
    public List<RecipeView> listArchivedRecipes(UUID groupId, UUID actorUserId) {
        ensureMealAccess(groupId, actorUserId);
        List<RecipeView> views = new ArrayList<>();
        for (Recipe recipe : recipeRepository.findArchivedByGroupId(groupId)) {
            views.add(toView(recipe, true));
        }
        views.sort((a, b) -> {
            int nameCompare = a.name().compareToIgnoreCase(b.name());
            if (nameCompare != 0) {
                return nameCompare;
            }
            return a.recipeId().compareTo(b.recipeId());
        });
        return views;
    }

    @Transactional(readOnly = true)
    public List<RecipeView> listRecentlyUsedRecipes(UUID groupId, UUID actorUserId) {
        ensureMealAccess(groupId, actorUserId);
        List<RecipeView> views = new ArrayList<>();
        for (Recipe recipe : findRecentlyUsedSavedRecipes(groupId)) {
            views.add(toView(recipe, false));
        }
        return views;
    }

    @Transactional(readOnly = true)
    public List<RecentPlannedMealView> listRecentPlannedMeals(UUID groupId, UUID actorUserId) {
        ensureMealAccess(groupId, actorUserId);
        LocalDate today = LocalDate.now(clock);
        int currentYear = today.get(WeekFields.ISO.weekBasedYear());
        int currentIsoWeek = today.get(WeekFields.ISO.weekOfWeekBasedYear());
        int currentDayOfWeek = today.getDayOfWeek().getValue();

        List<RecentPlannedMeal> orderedMeals = weekPlanRepository.findRecentMealsOnOrBefore(
                groupId,
                currentYear,
                currentIsoWeek,
                currentDayOfWeek
        );
        if (orderedMeals.isEmpty()) {
            return List.of();
        }

        Set<UUID> recipeIds = new HashSet<>();
        for (RecentPlannedMeal meal : orderedMeals) {
            if (meal.recipeId() != null) {
                recipeIds.add(meal.recipeId());
            }
        }

        Map<UUID, Recipe> recipesById = new HashMap<>();
        if (!recipeIds.isEmpty()) {
            for (Recipe recipe : recipeRepository.findByGroupIdAndIds(groupId, recipeIds)) {
                if (recipe.isArchived()) {
                    continue;
                }
                recipesById.put(recipe.getId(), recipe);
            }
        }

        Set<String> seenMealKeys = new HashSet<>();
        List<RecentPlannedMealView> views = new ArrayList<>();
        for (RecentPlannedMeal meal : orderedMeals) {
            UUID reusableRecipeId = meal.recipeId();
            if (reusableRecipeId != null && !recipesById.containsKey(reusableRecipeId)) {
                reusableRecipeId = null;
            }
            String dedupeKey = normalizeRecentMealKey(meal.mealTitle(), reusableRecipeId, meal.recipeTitleSnapshot());
            if (!seenMealKeys.add(dedupeKey)) {
                continue;
            }
            views.add(new RecentPlannedMealView(
                    meal.year(),
                    meal.isoWeek(),
                    meal.dayOfWeek(),
                    meal.mealType().name(),
                    meal.mealTitle(),
                    reusableRecipeId,
                    meal.recipeTitleSnapshot()
            ));
            if (views.size() >= RECENTLY_USED_MEALS_LIMIT) {
                break;
            }
        }
        return views;
    }

    @Transactional(readOnly = true)
    public List<RecentMealOccurrenceView> listRecentMealOccurrences(UUID groupId, UUID actorUserId, int limit) {
        ensureMealAccess(groupId, actorUserId);
        int resolvedLimit = normalizePositiveLimit(limit, 12);
        LocalDate today = LocalDate.now(clock);
        List<MealOccurrence> occurrences = loadHistoricalOccurrences(groupId, today);
        Map<UUID, Recipe> recipesById = recipesById(groupId);

        Set<String> seenIdentityKeys = new HashSet<>();
        List<RecentMealOccurrenceView> views = new ArrayList<>();
        for (MealOccurrence occurrence : occurrences) {
            MealIdentity identity = resolveMealIdentity(occurrence, recipesById);
            if (!seenIdentityKeys.add(identity.key())) {
                continue;
            }
            views.add(toRecentMealOccurrenceView(occurrence, identity));
            if (views.size() >= resolvedLimit) {
                break;
            }
        }
        return views;
    }

    @Transactional(readOnly = true)
    public List<MealIdentitySummaryView> listMealIdentitySummaries(UUID groupId, UUID actorUserId, int limit) {
        ensureMealAccess(groupId, actorUserId);
        int resolvedLimit = normalizePositiveLimit(limit, 12);
        LocalDate today = LocalDate.now(clock);
        List<MealUsageAggregate> aggregates = summarizeMealUsage(groupId, today);
        return aggregates.stream()
                .limit(resolvedLimit)
                .map(this::toMealIdentitySummaryView)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecipeUsageSummaryView> listRecipeUsageSummaries(UUID groupId, UUID actorUserId, int limit) {
        ensureMealAccess(groupId, actorUserId);
        int resolvedLimit = normalizePositiveLimit(limit, 12);
        LocalDate today = LocalDate.now(clock);
        List<RecipeUsageHistory> histories = summarizeRecipeUsage(groupId, today);
        return histories.stream()
                .limit(resolvedLimit)
                .map(this::toRecipeUsageSummaryView)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecipeUsageSummaryView getRecipeUsageSummary(UUID groupId, UUID actorUserId, UUID recipeId) {
        ensureMealAccess(groupId, actorUserId);
        if (recipeId == null) {
            throw new IllegalArgumentException("recipeId must not be null");
        }
        loadRecipe(groupId, recipeId);
        LocalDate today = LocalDate.now(clock);
        for (RecipeUsageHistory history : summarizeRecipeUsage(groupId, today)) {
            if (history.recipeId().equals(recipeId)) {
                return toRecipeUsageSummaryView(history);
            }
        }
        Recipe recipe = loadRecipe(groupId, recipeId);
        return new RecipeUsageSummaryView(
                recipe.getId(),
                recipe.getName(),
                LocalDate.ofInstant(recipe.getCreatedAt(), ZoneOffset.UTC),
                0,
                0,
                0,
                false,
                false,
                recipe.getMakeSoonAt() != null,
                false,
                false
        );
    }

    @Transactional(readOnly = true)
    public List<HouseholdPreferenceSummaryView> listHouseholdPreferenceSummaries(UUID groupId, UUID actorUserId) {
        ensureMealAccess(groupId, actorUserId);
        requireHouseholdPreferenceSignalRepository();
        return householdPreferenceSignalRepository.findByGroupId(groupId).stream()
                .sorted(java.util.Comparator.comparing(HouseholdPreferenceSignal::getCreatedAt))
                .map(this::toHouseholdPreferenceSummaryView)
                .toList();
    }

    @Transactional
    public HouseholdPreferenceSummaryView writeHouseholdPreferenceSignal(
            UUID groupId,
            UUID actorUserId,
            String targetKind,
            String signalType,
            UUID recipeId,
            String mealIdentityKey
    ) {
        ensureMealAccess(groupId, actorUserId);
        requireHouseholdPreferenceSignalRepository();
        HouseholdPreferenceSignalTargetKind resolvedTargetKind = parsePreferenceTargetKind(targetKind);
        HouseholdPreferenceSignalType resolvedSignalType = parsePreferenceSignalType(signalType);
        HouseholdPreferenceSignal existing = findExistingPreferenceSignal(
                groupId,
                resolvedTargetKind,
                resolvedSignalType,
                recipeId,
                mealIdentityKey
        ).orElse(null);
        if (existing != null) {
            return toHouseholdPreferenceSummaryView(existing);
        }
        Instant now = clock.instant();
        HouseholdPreferenceSignal signal = new HouseholdPreferenceSignal(
                UUID.randomUUID(),
                groupId,
                resolvedTargetKind,
                resolvedTargetKind == HouseholdPreferenceSignalTargetKind.RECIPE ? recipeId : null,
                resolvedTargetKind == HouseholdPreferenceSignalTargetKind.MEAL_IDENTITY ? normalizeMealIdentityKey(mealIdentityKey) : null,
                resolvedSignalType,
                now,
                now
        );
        return toHouseholdPreferenceSummaryView(householdPreferenceSignalRepository.save(signal));
    }

    @Transactional
    public void clearHouseholdPreferenceSignal(
            UUID groupId,
            UUID actorUserId,
            String targetKind,
            String signalType,
            UUID recipeId,
            String mealIdentityKey
    ) {
        ensureMealAccess(groupId, actorUserId);
        requireHouseholdPreferenceSignalRepository();
        findExistingPreferenceSignal(
                groupId,
                parsePreferenceTargetKind(targetKind),
                parsePreferenceSignalType(signalType),
                recipeId,
                mealIdentityKey
        ).ifPresent(householdPreferenceSignalRepository::delete);
    }

    @Transactional(readOnly = true)
    public PlanningChoiceSupportView getSlotPlanningChoiceSupport(
            UUID groupId,
            UUID actorUserId,
            int year,
            int isoWeek,
            int dayOfWeek,
            MealType mealType
    ) {
        ensureMealAccess(groupId, actorUserId);
        validateIsoWeek(year, isoWeek);
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new IllegalArgumentException("dayOfWeek must be between 1 and 7");
        }
        if (mealType == null) {
            throw new IllegalArgumentException("mealType must not be null");
        }
        PlanningContext context = new PlanningContext(
                PlanningScenario.SLOT,
                localDateForIsoWeek(year, isoWeek, dayOfWeek),
                year,
                isoWeek,
                dayOfWeek,
                mealType,
                null
        );
        return toPlanningChoiceSupportView(loadPlanningChoiceSupport(groupId, context));
    }

    @Transactional(readOnly = true)
    public PlanningChoiceSupportView getTonightPlanningChoiceSupport(UUID groupId, UUID actorUserId) {
        ensureMealAccess(groupId, actorUserId);
        LocalDate today = LocalDate.now(clock);
        PlanningContext context = new PlanningContext(
                PlanningScenario.TONIGHT,
                today,
                today.get(WeekFields.ISO.weekBasedYear()),
                today.get(WeekFields.ISO.weekOfWeekBasedYear()),
                today.getDayOfWeek().getValue(),
                MealType.DINNER,
                null
        );
        return toPlanningChoiceSupportView(loadPlanningChoiceSupport(groupId, context));
    }

    @Transactional(readOnly = true)
    public PlanningChoiceSupportView getWeekStartPlanningChoiceSupport(
            UUID groupId,
            UUID actorUserId,
            int year,
            int isoWeek
    ) {
        ensureMealAccess(groupId, actorUserId);
        validateIsoWeek(year, isoWeek);
        PlanningContext context = new PlanningContext(
                PlanningScenario.WEEK_START,
                localDateForIsoWeek(year, isoWeek, 1),
                year,
                isoWeek,
                1,
                null,
                null
        );
        return toPlanningChoiceSupportView(loadPlanningChoiceSupport(groupId, context));
    }

    @Transactional
    public RecipeView updateRecipe(
            UUID groupId,
            UUID actorUserId,
            UUID recipeId,
            String name,
            String sourceName,
            String sourceUrl,
            String originKind,
            String servings,
            String shortNote,
            String instructions,
            Boolean savedInRecipes,
            List<IngredientInput> ingredients
    ) {
        ensureMealAccess(groupId, actorUserId);
        Recipe existing = loadRecipe(groupId, recipeId);
        Recipe updated = new Recipe(
                existing.getId(),
                existing.getGroupId(),
                normalizeRecipeName(name),
                normalizeOptionalRecipeText(sourceName),
                sourceUrl == null ? existing.getSourceUrl() : normalizeOptionalRecipeUrl(sourceUrl),
                originKind == null ? existing.getOriginKind() : normalizeOriginKind(originKind),
                normalizeOptionalRecipeText(servings),
                existing.getMakeSoonAt(),
                normalizeOptionalRecipeText(shortNote),
                normalizeOptionalInstructions(instructions),
                existing.getCreatedAt(),
                clock.instant(),
                existing.getArchivedAt(),
                savedInRecipes == null ? existing.isSavedInRecipes() : savedInRecipes,
                toDomainIngredients(ingredients)
        );
        return toView(recipeRepository.save(updated), true);
    }

    @Transactional
    public RecipeView archiveRecipe(UUID groupId, UUID actorUserId, UUID recipeId) {
        ensureMealAccess(groupId, actorUserId);
        Recipe existing = loadRecipe(groupId, recipeId);
        if (existing.isArchived()) {
            return toView(existing, true);
        }
        Instant now = clock.instant();
        Recipe archived = new Recipe(
                existing.getId(),
                existing.getGroupId(),
                existing.getName(),
                existing.getSourceName(),
                existing.getSourceUrl(),
                existing.getOriginKind(),
                existing.getServings(),
                existing.getMakeSoonAt(),
                existing.getShortNote(),
                existing.getInstructions(),
                existing.getCreatedAt(),
                now,
                now,
                existing.isSavedInRecipes(),
                existing.getIngredients()
        );
        return toView(recipeRepository.save(archived), true);
    }

    @Transactional
    public RecipeView restoreRecipe(UUID groupId, UUID actorUserId, UUID recipeId) {
        ensureMealAccess(groupId, actorUserId);
        Recipe existing = loadRecipe(groupId, recipeId);
        if (!existing.isArchived()) {
            return toView(existing, true);
        }
        Instant now = clock.instant();
        Recipe restored = new Recipe(
                existing.getId(),
                existing.getGroupId(),
                existing.getName(),
                existing.getSourceName(),
                existing.getSourceUrl(),
                existing.getOriginKind(),
                existing.getServings(),
                existing.getMakeSoonAt(),
                existing.getShortNote(),
                existing.getInstructions(),
                existing.getCreatedAt(),
                now,
                null,
                existing.isSavedInRecipes(),
                existing.getIngredients()
        );
        return toView(recipeRepository.save(restored), true);
    }

    @Transactional
    public void deleteRecipe(UUID groupId, UUID actorUserId, UUID recipeId) {
        ensureMealAccess(groupId, actorUserId);
        Recipe existing = loadRecipe(groupId, recipeId);
        DeleteEligibility deleteEligibility = getDeleteEligibility(groupId, existing);
        if (!deleteEligibility.eligible()) {
            throw new RecipeDeleteBlockedException(deleteEligibility.blockedReason());
        }
        recipeRepository.delete(existing);
    }

    @Transactional
    public AddMealOutput addOrReplaceMeal(
            UUID groupId,
            UUID actorUserId,
            int year,
            int isoWeek,
            int dayOfWeek,
            MealType mealType,
            String mealTitle,
            UUID recipeId,
            UUID targetShoppingListId,
            List<Integer> selectedIngredientPositions
    ) {
        ensureMealAccess(groupId, actorUserId);
        validateIsoWeek(year, isoWeek);
        Recipe recipe = recipeId == null ? null : loadRecipe(groupId, recipeId);
        String normalizedMealTitle = normalizeMealTitle(mealTitle, recipe);
        WeekPlan weekPlan = weekPlanRepository.findByGroupAndWeek(groupId, year, isoWeek)
                .orElseGet(() -> new WeekPlan(
                        UUID.randomUUID(),
                        groupId,
                        year,
                        isoWeek,
                        clock.instant()
                ));
        PlannedMeal existingMeal = weekPlan.getMeal(dayOfWeek, mealType);
        boolean shouldPushToShopping = recipe != null && targetShoppingListId != null;
        Instant shoppingHandledAt = null;
        UUID shoppingListId = null;
        if (shouldPushToShopping) {
            shoppingHandledAt = clock.instant();
            shoppingListId = targetShoppingListId;
        } else if (existingMeal != null && sameMealContent(
                existingMeal,
                normalizedMealTitle,
                recipeId,
                recipe == null ? null : recipe.getName()
        )) {
            shoppingHandledAt = existingMeal.getShoppingHandledAt();
            shoppingListId = existingMeal.getShoppingListId();
        }

        weekPlan.addOrReplaceMeal(
                dayOfWeek,
                mealType,
                normalizedMealTitle,
                recipeId,
                recipe == null ? null : recipe.getName(),
                shoppingHandledAt,
                shoppingListId
        );
        WeekPlan saved = weekPlanRepository.save(weekPlan);

        if (shouldPushToShopping) {
            // V0.5c intent: recipe ingredients primarily act as shopping-item generators.
            pushIngredientsToShopping(
                    groupId,
                    actorUserId,
                    targetShoppingListId,
                    recipe.getName(),
                    recipe.getIngredients(),
                    selectedIngredientPositions
            );
        }

        PlannedMeal savedMeal = saved.getMealOrThrow(dayOfWeek, mealType);
        PlannedMealView mealView = new PlannedMealView(
                savedMeal.getDayOfWeek(),
                savedMeal.getMealType().name(),
                savedMeal.getRecipeId(),
                savedMeal.getMealTitle(),
                savedMeal.getRecipeTitleSnapshot(),
                savedMeal.getShoppingHandledAt(),
                savedMeal.getShoppingListId()
        );
        return new AddMealOutput(saved.getId(), saved.getYear(), saved.getIsoWeek(), mealView);
    }

    @Transactional
    public AddMealOutput addOrReplaceMeal(
            UUID groupId,
            UUID actorUserId,
            int year,
            int isoWeek,
            int dayOfWeek,
            MealType mealType,
            UUID recipeId,
            UUID targetShoppingListId,
            List<Integer> selectedIngredientPositions
    ) {
        return addOrReplaceMeal(
                groupId,
                actorUserId,
                year,
                isoWeek,
                dayOfWeek,
                mealType,
                null,
                recipeId,
                targetShoppingListId,
                selectedIngredientPositions
        );
    }

    @Transactional
    public void removeMeal(
            UUID groupId,
            UUID actorUserId,
            int year,
            int isoWeek,
            int dayOfWeek,
            MealType mealType
    ) {
        ensureMealAccess(groupId, actorUserId);
        validateIsoWeek(year, isoWeek);
        WeekPlan weekPlan = weekPlanRepository.findByGroupAndWeek(groupId, year, isoWeek)
                .orElseThrow(() -> new MealNotFoundException("Meal not found"));
        try {
            weekPlan.removeMeal(dayOfWeek, mealType);
        } catch (IllegalArgumentException ex) {
            throw new MealNotFoundException("Meal not found");
        }
        weekPlanRepository.save(weekPlan);
    }

    @Transactional(readOnly = true)
    public WeekPlanView getWeekPlan(
            UUID groupId,
            UUID actorUserId,
            int year,
            int isoWeek
    ) {
        ensureMealAccess(groupId, actorUserId);
        validateIsoWeek(year, isoWeek);
        return weekPlanRepository.findByGroupAndWeek(groupId, year, isoWeek)
                .map(this::toView)
                .orElseGet(() -> new WeekPlanView(null, year, isoWeek, null, false, List.of()));
    }

    @Transactional(readOnly = true)
    public MealShoppingProjectionView getMealShoppingProjection(
            UUID groupId,
            UUID actorUserId,
            int year,
            int isoWeek,
            int dayOfWeek,
            MealType mealType,
            UUID shoppingListId
    ) {
        ensureMealAccess(groupId, actorUserId);
        validateIsoWeek(year, isoWeek);
        WeekPlan weekPlan = weekPlanRepository.findByGroupAndWeek(groupId, year, isoWeek)
                .orElseThrow(() -> new MealNotFoundException("Meal not found"));
        PlannedMeal meal = weekPlan.getMeal(dayOfWeek, mealType);
        if (meal == null) {
            throw new MealNotFoundException("Meal not found");
        }
        UUID assessedShoppingListId = shoppingListId != null ? shoppingListId : meal.getShoppingListId();
        String assessedShoppingListName = null;
        if (assessedShoppingListId != null) {
            MealsShoppingListSnapshot assessedShoppingList = loadShoppingListSnapshots(
                    groupId,
                    actorUserId,
                    Set.of(assessedShoppingListId)
            ).get(assessedShoppingListId);
            if (assessedShoppingList != null) {
                assessedShoppingListName = assessedShoppingList.listName();
            }
        }
        return toMealShoppingProjectionView(
                buildMealShoppingProjection(groupId, actorUserId, year, isoWeek, meal, shoppingListId),
                assessedShoppingListId,
                assessedShoppingListName
        );
    }

    @Transactional(readOnly = true)
    public WeekShoppingProjectionView getWeekShoppingProjection(
            UUID groupId,
            UUID actorUserId,
            int year,
            int isoWeek
    ) {
        ensureMealAccess(groupId, actorUserId);
        validateIsoWeek(year, isoWeek);
        WeekPlan weekPlan = weekPlanRepository.findByGroupAndWeek(groupId, year, isoWeek)
                .orElse(null);
        if (weekPlan == null) {
            return toWeekShoppingProjectionView(new WeekShoppingProjection(
                    null,
                    year,
                    isoWeek,
                    0,
                    0,
                    0,
                    0,
                    new ShoppingDelta(List.of()),
                    List.of()
            ));
        }

        List<MealShoppingProjection> meals = new ArrayList<>();
        for (PlannedMeal meal : weekPlan.getMeals()) {
            meals.add(buildMealShoppingProjection(groupId, actorUserId, year, isoWeek, meal, null));
        }
        return toWeekShoppingProjectionView(kitchenReadinessEngine.buildWeekProjection(
                weekPlan.getId(),
                year,
                isoWeek,
                meals
        ));
    }

    @Transactional(readOnly = true)
    public WeekShoppingReviewView getWeekShoppingReview(
            UUID groupId,
            UUID actorUserId,
            int year,
            int isoWeek,
            UUID shoppingListId
    ) {
        ensureMealAccess(groupId, actorUserId);
        validateIsoWeek(year, isoWeek);
        WeekPlan weekPlan = weekPlanRepository.findByGroupAndWeek(groupId, year, isoWeek)
                .orElse(null);
        return toWeekShoppingReviewView(
                buildWeekShoppingReview(groupId, actorUserId, year, isoWeek, weekPlan, shoppingListId)
        );
    }

    @Transactional
    public WeekShoppingReviewView addWeekShoppingReviewLines(
            UUID groupId,
            UUID actorUserId,
            int year,
            int isoWeek,
            UUID shoppingListId,
            List<String> selectedLineIds
    ) {
        ensureMealAccess(groupId, actorUserId);
        validateIsoWeek(year, isoWeek);
        if (shoppingListId == null) {
            throw new IllegalArgumentException("shoppingListId must not be null");
        }
        if (selectedLineIds == null) {
            throw new IllegalArgumentException("selectedLineIds must not be null");
        }

        WeekPlan weekPlan = weekPlanRepository.findByGroupAndWeek(groupId, year, isoWeek)
                .orElse(null);
        ResolvedWeekShoppingReview initialReview = buildWeekShoppingReview(
                groupId,
                actorUserId,
                year,
                isoWeek,
                weekPlan,
                shoppingListId
        );
        WeekShoppingReview review = initialReview.review();
        Map<String, AggregatedIngredientComparison> addableById = new HashMap<>();
        for (AggregatedIngredientComparison ingredient : review.ingredients()) {
            if (ingredient.state() == AggregatedIngredientComparisonState.ADD_TO_LIST) {
                addableById.put(ingredient.need().lineId(), ingredient);
            }
        }

        for (String selectedLineId : selectedLineIds) {
            if (selectedLineId == null || selectedLineId.isBlank()) {
                throw new IllegalArgumentException("selectedLineIds must not contain blanks");
            }
            if (!addableById.containsKey(selectedLineId)) {
                throw new IllegalArgumentException("Unknown week shopping review line: " + selectedLineId);
            }
        }

        for (String selectedLineId : selectedLineIds) {
            AggregatedIngredientComparison ingredient = addableById.get(selectedLineId);
            AggregatedIngredientNeed need = ingredient.need();
            java.math.BigDecimal quantityToAdd = ingredient.remainingQuantity() != null
                    ? ingredient.remainingQuantity()
                    : need.totalQuantity();
            mealsShoppingPort.addShoppingItem(
                    groupId,
                    actorUserId,
                    shoppingListId,
                    need.normalizedShoppingName(),
                    quantityToAdd,
                    need.unitName(),
                    "meal-plan",
                    "Week " + isoWeek + " meals"
            );
        }

        if (weekPlan != null) {
            weekPlan.rememberShoppingReview(shoppingListId, clock.instant());
            weekPlanRepository.save(weekPlan);
        }

        WeekPlan refreshedWeekPlan = weekPlanRepository.findByGroupAndWeek(groupId, year, isoWeek)
                .orElse(weekPlan);
        return toWeekShoppingReviewView(
                buildWeekShoppingReview(groupId, actorUserId, year, isoWeek, refreshedWeekPlan, shoppingListId)
        );
    }

    private WeekPlanView toView(WeekPlan weekPlan) {
        Set<UUID> recipeIds = new HashSet<>();
        for (PlannedMeal meal : weekPlan.getMeals()) {
            if (meal.getRecipeId() != null) {
                recipeIds.add(meal.getRecipeId());
            }
        }

        Map<UUID, Recipe> recipesById = new HashMap<>();
        Map<UUID, String> namesByRecipeId = new HashMap<>();
        for (Recipe recipe : recipeRepository.findByGroupIdAndIds(weekPlan.getGroupId(), recipeIds)) {
            recipesById.put(recipe.getId(), recipe);
            namesByRecipeId.put(recipe.getId(), recipe.getName());
        }

        boolean hasReviewableWeekShopping = false;
        for (PlannedMeal meal : weekPlan.getMeals()) {
            if (meal.getRecipeId() == null) {
                continue;
            }
            Recipe recipe = recipesById.get(meal.getRecipeId());
            if (recipe != null && !projectMealIngredientNeeds(recipe).isEmpty()) {
                hasReviewableWeekShopping = true;
                break;
            }
        }

        List<PlannedMealView> meals = new ArrayList<>();
        for (PlannedMeal meal : weekPlan.getMeals()) {
            meals.add(new PlannedMealView(
                    meal.getDayOfWeek(),
                    meal.getMealType().name(),
                    meal.getRecipeId(),
                    meal.getMealTitle(),
                    meal.getRecipeId() == null
                            ? null
                            : namesByRecipeId.getOrDefault(meal.getRecipeId(), meal.getRecipeTitleSnapshot()),
                    meal.getShoppingHandledAt(),
                    meal.getShoppingListId()
            ));
        }
        return new WeekPlanView(
                weekPlan.getId(),
                weekPlan.getYear(),
                weekPlan.getIsoWeek(),
                weekPlan.getCreatedAt(),
                hasReviewableWeekShopping,
                meals
        );
    }

    private ResolvedWeekShoppingReview buildWeekShoppingReview(
            UUID groupId,
            UUID actorUserId,
            int year,
            int isoWeek,
            WeekPlan weekPlan,
            UUID shoppingListId
    ) {
        UUID rememberedListId = weekPlan == null ? null : weekPlan.getShoppingReviewListId();
        UUID assessedShoppingListId = shoppingListId != null ? shoppingListId : rememberedListId;
        Set<UUID> requestedShoppingListIds = new HashSet<>();
        if (assessedShoppingListId != null) {
            requestedShoppingListIds.add(assessedShoppingListId);
        }
        if (rememberedListId != null) {
            requestedShoppingListIds.add(rememberedListId);
        }
        Map<UUID, MealsShoppingListSnapshot> shoppingListsById = loadShoppingListSnapshots(
                groupId,
                actorUserId,
                requestedShoppingListIds
        );
        MealsShoppingListSnapshot assessedShoppingList = assessedShoppingListId == null
                ? null
                : shoppingListsById.get(assessedShoppingListId);
        if (shoppingListId != null && assessedShoppingList == null) {
            throw new MealsShoppingListNotFoundException("list not found: " + assessedShoppingListId);
        }
        if (shoppingListId == null && assessedShoppingList == null) {
            assessedShoppingListId = null;
        }
        WeekShoppingReviewLink reviewLink = null;
        if (weekPlan != null
                && weekPlan.getShoppingReviewListId() != null
                && weekPlan.getShoppingReviewHandledAt() != null) {
            reviewLink = new WeekShoppingReviewLink(
                    weekPlan.getShoppingReviewListId(),
                    weekPlan.getShoppingReviewHandledAt()
            );
        }
        return new ResolvedWeekShoppingReview(
                weekShoppingReviewEngine.buildWeekReview(
                        weekPlan == null ? null : weekPlan.getId(),
                        year,
                        isoWeek,
                        reviewLink,
                        buildWeekIngredientOccurrences(groupId, weekPlan),
                        assessedShoppingList
                ),
                shoppingListsById
        );
    }

    private List<WeekShoppingReviewEngine.WeekIngredientOccurrence> buildWeekIngredientOccurrences(
            UUID groupId,
            WeekPlan weekPlan
    ) {
        if (weekPlan == null) {
            return List.of();
        }
        Set<UUID> recipeIds = new HashSet<>();
        for (PlannedMeal meal : weekPlan.getMeals()) {
            if (meal.getRecipeId() != null) {
                recipeIds.add(meal.getRecipeId());
            }
        }

        Map<UUID, Recipe> recipesById = new HashMap<>();
        for (Recipe recipe : recipeRepository.findByGroupIdAndIds(groupId, recipeIds)) {
            recipesById.put(recipe.getId(), recipe);
        }

        List<WeekShoppingReviewEngine.WeekIngredientOccurrence> occurrences = new ArrayList<>();
        for (PlannedMeal meal : weekPlan.getMeals()) {
            if (meal.getRecipeId() == null) {
                continue;
            }
            Recipe recipe = recipesById.get(meal.getRecipeId());
            if (recipe == null) {
                continue;
            }
            for (MealIngredientNeed need : projectMealIngredientNeeds(recipe)) {
                occurrences.add(new WeekShoppingReviewEngine.WeekIngredientOccurrence(
                        meal.getDayOfWeek(),
                        meal.getMealType(),
                        meal.getMealTitle(),
                        need
                ));
            }
        }
        occurrences.sort(Comparator
                .comparingInt(WeekShoppingReviewEngine.WeekIngredientOccurrence::dayOfWeek)
                .thenComparing(occurrence -> occurrence.mealType().ordinal())
                .thenComparing(occurrence -> occurrence.need().position()));
        return List.copyOf(occurrences);
    }

    private MealShoppingProjection buildMealShoppingProjection(
            UUID groupId,
            UUID actorUserId,
            int year,
            int isoWeek,
            PlannedMeal meal,
            UUID shoppingListId
    ) {
        UUID linkedShoppingListId = meal.getShoppingListId();
        UUID assessedShoppingListId = shoppingListId != null ? shoppingListId : linkedShoppingListId;
        Set<UUID> requestedSnapshotIds = new HashSet<>();
        if (linkedShoppingListId != null) {
            requestedSnapshotIds.add(linkedShoppingListId);
        }
        if (assessedShoppingListId != null) {
            requestedSnapshotIds.add(assessedShoppingListId);
        }
        Map<UUID, MealsShoppingListSnapshot> shoppingListsById = loadShoppingListSnapshots(
                groupId,
                actorUserId,
                requestedSnapshotIds
        );
        MealsShoppingListSnapshot linkedShoppingList = linkedShoppingListId == null
                ? null
                : shoppingListsById.get(linkedShoppingListId);
        MealsShoppingListSnapshot assessedShoppingList = assessedShoppingListId == null
                ? null
                : shoppingListsById.get(assessedShoppingListId);
        Recipe recipe = meal.getRecipeId() == null ? null : loadRecipe(groupId, meal.getRecipeId());
        return kitchenReadinessEngine.buildMealProjection(
                year,
                isoWeek,
                meal,
                buildShoppingLinkReference(meal, linkedShoppingList),
                projectMealIngredientNeeds(recipe),
                assessedShoppingList
        );
    }

    private Map<UUID, MealsShoppingListSnapshot> loadShoppingListSnapshots(
            UUID groupId,
            UUID actorUserId,
            Set<UUID> shoppingListIds
    ) {
        if (shoppingListIds == null || shoppingListIds.isEmpty()) {
            return Map.of();
        }
        return mealsShoppingPort.listShoppingListSnapshots(groupId, actorUserId, shoppingListIds);
    }

    private ShoppingLinkReference buildShoppingLinkReference(
            PlannedMeal meal,
            MealsShoppingListSnapshot shoppingListSnapshot
    ) {
        if (meal.getShoppingListId() == null) {
            return new ShoppingLinkReference(null, null, null, ShoppingLinkStatus.NOT_LINKED);
        }
        if (shoppingListSnapshot == null) {
            return new ShoppingLinkReference(
                    meal.getShoppingListId(),
                    null,
                    meal.getShoppingHandledAt(),
                    ShoppingLinkStatus.MISSING_LIST
            );
        }
        return new ShoppingLinkReference(
                shoppingListSnapshot.listId(),
                shoppingListSnapshot.listName(),
                meal.getShoppingHandledAt(),
                ShoppingLinkStatus.LINKED
        );
    }

    private List<MealIngredientNeed> projectMealIngredientNeeds(Recipe recipe) {
        if (recipe == null) {
            return List.of();
        }
        List<MealIngredientNeed> needs = new ArrayList<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
            ShoppingIngredientProjection projection = projectIngredientForShopping(ingredient);
            needs.add(new MealIngredientNeed(
                    ingredient.getId(),
                    ingredient.getPosition(),
                    ingredient.getName(),
                    projection.name(),
                    ingredient.getRawText(),
                    projection.quantity(),
                    projection.unitName()
            ));
        }
        needs.sort((a, b) -> Integer.compare(a.position(), b.position()));
        return List.copyOf(needs);
    }

    private Recipe loadRecipe(UUID groupId, UUID recipeId) {
        if (recipeId == null) {
            throw new IllegalArgumentException("recipeId must not be null");
        }
        return recipeRepository.findByIdAndGroupId(recipeId, groupId)
                .orElseThrow(() -> new RecipeNotFoundException(recipeId));
    }

    private RecipeDraft loadRecipeDraft(UUID groupId, UUID draftId) {
        requireRecipeDraftRepository();
        if (draftId == null) {
            throw new IllegalArgumentException("draftId must not be null");
        }
        return recipeDraftRepository.findByIdAndGroupId(draftId, groupId)
                .orElseThrow(() -> new RecipeDraftNotFoundException(draftId));
    }

    private void requireRecipeDraftRepository() {
        if (recipeDraftRepository == null) {
            throw new IllegalStateException("Recipe draft platform is not configured");
        }
    }

    private void requireRecipeImportPort() {
        if (recipeImportPort == null) {
            throw new IllegalStateException("Recipe import platform is not configured");
        }
    }

    private void requireRecipeAssetIntakePort() {
        if (recipeAssetIntakePort == null) {
            throw new RecipeAssetIntakeUnavailableException("Recipe file and image import is not available yet");
        }
    }

    private void requireRecipeDocumentAssetStore() {
        if (recipeDocumentAssetStore == null) {
            throw new RecipeAssetIntakeUnavailableException("Recipe file import is not available yet");
        }
    }

    private void requireMealMemoryRepository() {
        if (mealMemoryRepository == null) {
            throw new IllegalStateException("Meal memory platform is not configured");
        }
    }

    private void requireHouseholdPreferenceSignalRepository() {
        if (householdPreferenceSignalRepository == null) {
            throw new IllegalStateException("Household preference platform is not configured");
        }
    }

    private List<MealOccurrence> loadHistoricalOccurrences(UUID groupId, LocalDate referenceDate) {
        requireMealMemoryRepository();
        int year = referenceDate.get(WeekFields.ISO.weekBasedYear());
        int isoWeek = referenceDate.get(WeekFields.ISO.weekOfWeekBasedYear());
        int dayOfWeek = referenceDate.getDayOfWeek().getValue();
        return mealMemoryRepository.findHistoricalOccurrencesOnOrBefore(groupId, year, isoWeek, dayOfWeek);
    }

    private List<HouseholdPreferenceSignal> loadPreferenceSignals(UUID groupId) {
        requireHouseholdPreferenceSignalRepository();
        return householdPreferenceSignalRepository.findByGroupId(groupId);
    }

    private Map<UUID, Recipe> recipesById(UUID groupId) {
        Map<UUID, Recipe> recipesById = new HashMap<>();
        for (Recipe recipe : recipeRepository.findByGroupId(groupId)) {
            recipesById.put(recipe.getId(), recipe);
        }
        return recipesById;
    }

    private List<MealUsageAggregate> summarizeMealUsage(UUID groupId, LocalDate referenceDate) {
        return mealChoiceSupportEngine.summarizeMealUsage(
                loadHistoricalOccurrences(groupId, referenceDate),
                recipesById(groupId).values(),
                loadPreferenceSignals(groupId),
                referenceDate
        );
    }

    private List<RecipeUsageHistory> summarizeRecipeUsage(UUID groupId, LocalDate referenceDate) {
        return mealChoiceSupportEngine.summarizeRecipeUsage(
                loadHistoricalOccurrences(groupId, referenceDate),
                recipesById(groupId).values(),
                loadPreferenceSignals(groupId),
                referenceDate
        );
    }

    private PlanningChoiceSupport loadPlanningChoiceSupport(UUID groupId, PlanningContext context) {
        return mealChoiceSupportEngine.buildPlanningChoiceSupport(
                context,
                loadHistoricalOccurrences(groupId, context.referenceDate()),
                recipesById(groupId).values(),
                loadPreferenceSignals(groupId)
        );
    }

    private MealIdentity resolveMealIdentity(MealOccurrence occurrence, Map<UUID, Recipe> recipesById) {
        if (occurrence.recipeId() != null) {
            Recipe recipe = recipesById.get(occurrence.recipeId());
            String title = recipe != null
                    ? recipe.getName()
                    : occurrence.recipeTitleSnapshot() == null ? occurrence.mealTitle() : occurrence.recipeTitleSnapshot();
            return MealIdentity.forRecipe(occurrence.recipeId(), title);
        }
        return MealIdentity.forTitle(occurrence.mealTitle());
    }

    private RecentMealOccurrenceView toRecentMealOccurrenceView(MealOccurrence occurrence, MealIdentity identity) {
        return new RecentMealOccurrenceView(
                occurrence.weekPlanId(),
                occurrence.year(),
                occurrence.isoWeek(),
                occurrence.dayOfWeek(),
                occurrence.mealType().name(),
                occurrence.plannedDate(),
                occurrence.mealTitle(),
                identity.key(),
                toMealIdentityKindValue(identity.kind()),
                occurrence.recipeId(),
                identity.kind() == MealIdentityKind.RECIPE ? identity.title() : occurrence.recipeTitleSnapshot()
        );
    }

    private MealIdentitySummaryView toMealIdentitySummaryView(MealUsageAggregate aggregate) {
        Set<String> usedMealTypes = new HashSet<>();
        for (MealType mealType : aggregate.usedMealTypes()) {
            usedMealTypes.add(mealType.name());
        }
        return new MealIdentitySummaryView(
                aggregate.identity().key(),
                toMealIdentityKindValue(aggregate.identity().kind()),
                aggregate.identity().title(),
                aggregate.identity().recipeId(),
                aggregate.lastPlannedDate(),
                aggregate.totalOccurrences(),
                aggregate.recentOccurrences(),
                aggregate.distinctWeeks(),
                Set.copyOf(usedMealTypes),
                aggregate.recent(),
                aggregate.frequent(),
                aggregate.familiar(),
                aggregate.fallback(),
                aggregate.preferenceFit(),
                aggregate.deprioritized(),
                aggregate.makeSoon()
        );
    }

    private RecipeUsageSummaryView toRecipeUsageSummaryView(RecipeUsageHistory history) {
        return new RecipeUsageSummaryView(
                history.recipeId(),
                history.recipeTitle(),
                history.lastUsedDate(),
                history.totalUses(),
                history.recentUses(),
                history.distinctWeeks(),
                history.frequent(),
                history.familiar(),
                history.makeSoon(),
                history.preferenceFit(),
                history.deprioritized()
        );
    }

    private HouseholdPreferenceSummaryView toHouseholdPreferenceSummaryView(HouseholdPreferenceSignal signal) {
        return new HouseholdPreferenceSummaryView(
                signal.getId(),
                toPreferenceTargetKindValue(signal.getTargetKind()),
                signal.getRecipeId(),
                signal.getMealIdentityKey(),
                toPreferenceSignalTypeValue(signal.getSignalType()),
                signal.getCreatedAt(),
                signal.getUpdatedAt()
        );
    }

    private PlanningChoiceSupportView toPlanningChoiceSupportView(PlanningChoiceSupport support) {
        PlanningContext context = support.context();
        return new PlanningChoiceSupportView(
                context.scenario().name().toLowerCase(Locale.ROOT),
                context.referenceDate(),
                context.year(),
                context.isoWeek(),
                context.dayOfWeek(),
                context.mealType() == null ? null : context.mealType().name(),
                context.recipeId(),
                support.recentCandidates().stream().map(this::toMealChoiceCandidateView).toList(),
                support.familiarCandidates().stream().map(this::toMealChoiceCandidateView).toList(),
                support.fallbackCandidates().stream().map(this::toMealChoiceCandidateView).toList(),
                support.makeSoonCandidates().stream().map(this::toMealChoiceCandidateView).toList()
        );
    }

    private MealShoppingProjectionView toMealShoppingProjectionView(
            MealShoppingProjection projection,
            UUID assessedShoppingListId,
            String assessedShoppingListName
    ) {
        return new MealShoppingProjectionView(
                projection.year(),
                projection.isoWeek(),
                projection.dayOfWeek(),
                projection.mealType().name(),
                projection.mealTitle(),
                projection.recipeId(),
                projection.recipeTitle(),
                projection.recipeBacked(),
                assessedShoppingListId,
                assessedShoppingListName,
                toShoppingLinkReferenceView(projection.shoppingLink()),
                toMealReadinessView(projection.readiness()),
                toShoppingDeltaView(projection.delta()),
                projection.ingredientCoverage().stream().map(this::toIngredientCoverageView).toList()
        );
    }

    private MealShoppingProjectionView toMealShoppingProjectionView(MealShoppingProjection projection) {
        return toMealShoppingProjectionView(
                projection,
                projection.shoppingLink().shoppingListId(),
                projection.shoppingLink().shoppingListName()
        );
    }

    private WeekShoppingProjectionView toWeekShoppingProjectionView(WeekShoppingProjection projection) {
        return new WeekShoppingProjectionView(
                projection.weekPlanId(),
                projection.year(),
                projection.isoWeek(),
                projection.mealsNeedingShoppingCount(),
                projection.partiallyReadyMealCount(),
                projection.readyFromShoppingViewMealCount(),
                projection.readinessUnclearMealCount(),
                toShoppingDeltaView(projection.delta()),
                projection.meals().stream().map(this::toMealShoppingProjectionView).toList()
        );
    }

    private WeekShoppingReviewView toWeekShoppingReviewView(ResolvedWeekShoppingReview resolvedReview) {
        WeekShoppingReview review = resolvedReview.review();
        Map<UUID, MealsShoppingListSnapshot> shoppingListsById = resolvedReview.shoppingListsById();
        String assessedShoppingListName = review.assessedShoppingListId() == null
                ? null
                : shoppingListsById.containsKey(review.assessedShoppingListId())
                        ? shoppingListsById.get(review.assessedShoppingListId()).listName()
                        : null;
        String linkedReviewListName = review.reviewLink() == null
                ? null
                : shoppingListsById.containsKey(review.reviewLink().shoppingListId())
                        ? shoppingListsById.get(review.reviewLink().shoppingListId()).listName()
                        : null;
        return new WeekShoppingReviewView(
                review.weekPlanId(),
                review.year(),
                review.isoWeek(),
                review.assessedShoppingListId(),
                assessedShoppingListName,
                review.reviewLink() == null
                        ? null
                        : new WeekShoppingReviewLinkView(
                                review.reviewLink().shoppingListId(),
                                linkedReviewListName,
                                review.reviewLink().reviewedAt()
                        ),
                review.ingredients().stream().map(this::toAggregatedIngredientComparisonView).toList()
        );
    }

    private AggregatedIngredientComparisonView toAggregatedIngredientComparisonView(
            AggregatedIngredientComparison comparison
    ) {
        return new AggregatedIngredientComparisonView(
                toAggregatedIngredientNeedView(comparison.need()),
                comparison.state().name().toLowerCase(Locale.ROOT),
                comparison.quantityOnList(),
                comparison.remainingQuantity()
        );
    }

    private AggregatedIngredientNeedView toAggregatedIngredientNeedView(AggregatedIngredientNeed need) {
        return new AggregatedIngredientNeedView(
                need.lineId(),
                need.ingredientName(),
                need.normalizedShoppingName(),
                need.totalQuantity(),
                need.unitName(),
                need.quantityConfidence().name().toLowerCase(Locale.ROOT),
                need.contributors().stream()
                        .map(contributor -> new ContributorMealReferenceView(
                                contributor.dayOfWeek(),
                                contributor.mealType().name(),
                                contributor.mealTitle()
                        ))
                        .toList()
        );
    }

    private record ResolvedWeekShoppingReview(
            WeekShoppingReview review,
            Map<UUID, MealsShoppingListSnapshot> shoppingListsById
    ) {
    }

    private ShoppingLinkReferenceView toShoppingLinkReferenceView(ShoppingLinkReference reference) {
        return new ShoppingLinkReferenceView(
                reference.shoppingListId(),
                reference.shoppingListName(),
                reference.shoppingHandledAt(),
                toShoppingLinkStatusValue(reference.status())
        );
    }

    private MealReadinessView toMealReadinessView(MealReadinessSignal signal) {
        return new MealReadinessView(
                toMealReadinessStateValue(signal.state()),
                signal.coveredIngredientCount(),
                signal.partiallyCoveredIngredientCount(),
                signal.missingIngredientCount(),
                signal.unknownIngredientCount(),
                signal.boughtIngredientCount(),
                signal.toBuyIngredientCount()
        );
    }

    private ShoppingDeltaView toShoppingDeltaView(ShoppingDelta delta) {
        int partialCount = 0;
        int missingCount = 0;
        int unknownCount = 0;
        for (IngredientCoverage coverage : delta.unresolvedIngredients()) {
            switch (coverage.state()) {
                case PARTIALLY_COVERED -> partialCount++;
                case MISSING -> missingCount++;
                case UNKNOWN -> unknownCount++;
                case COVERED -> { }
            }
        }
        return new ShoppingDeltaView(
                delta.unresolvedIngredients().size(),
                partialCount,
                missingCount,
                unknownCount,
                delta.unresolvedIngredients().stream().map(this::toIngredientCoverageView).toList()
        );
    }

    private IngredientCoverageView toIngredientCoverageView(IngredientCoverage coverage) {
        MealIngredientNeed need = coverage.need();
        return new IngredientCoverageView(
                new MealIngredientNeedView(
                        need.ingredientId(),
                        need.position(),
                        need.ingredientName(),
                        need.normalizedShoppingName(),
                        need.rawText(),
                        need.quantity(),
                        need.unitName()
                ),
                toIngredientCoverageStateValue(coverage.state()),
                toShoppingCoverageStateValue(coverage.shoppingState()),
                coverage.matchingItemCount(),
                coverage.coveredQuantity(),
                coverage.uncoveredQuantity(),
                coverage.uncertaintyReason()
        );
    }

    private MealChoiceCandidateView toMealChoiceCandidateView(ReuseCandidate candidate) {
        return new MealChoiceCandidateView(
                candidate.family().name().toLowerCase(Locale.ROOT),
                candidate.identity().key(),
                toMealIdentityKindValue(candidate.identity().kind()),
                candidate.identity().title(),
                candidate.identity().recipeId(),
                candidate.lastPlannedDate(),
                candidate.totalOccurrences(),
                candidate.recent(),
                candidate.frequent(),
                candidate.familiar(),
                candidate.fallback(),
                candidate.slotFit(),
                candidate.preferenceFit(),
                candidate.deprioritized(),
                candidate.makeSoon(),
                candidate.surfacedBecause()
        );
    }

    private int normalizePositiveLimit(int value, int fallback) {
        return value <= 0 ? fallback : value;
    }

    private HouseholdPreferenceSignalTargetKind parsePreferenceTargetKind(String value) {
        if (value == null) {
            throw new IllegalArgumentException("targetKind must not be null");
        }
        try {
            return HouseholdPreferenceSignalTargetKind.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown household preference targetKind: " + value);
        }
    }

    private HouseholdPreferenceSignalType parsePreferenceSignalType(String value) {
        if (value == null) {
            throw new IllegalArgumentException("signalType must not be null");
        }
        try {
            return HouseholdPreferenceSignalType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown household preference signalType: " + value);
        }
    }

    private java.util.Optional<HouseholdPreferenceSignal> findExistingPreferenceSignal(
            UUID groupId,
            HouseholdPreferenceSignalTargetKind targetKind,
            HouseholdPreferenceSignalType signalType,
            UUID recipeId,
            String mealIdentityKey
    ) {
        return switch (targetKind) {
            case RECIPE -> {
                if (recipeId == null) {
                    throw new IllegalArgumentException("recipeId must not be null for recipe preference signals");
                }
                loadRecipe(groupId, recipeId);
                yield householdPreferenceSignalRepository.findByRecipeTarget(groupId, recipeId, signalType);
            }
            case MEAL_IDENTITY -> {
                String normalizedIdentityKey = normalizeMealIdentityKey(mealIdentityKey);
                yield householdPreferenceSignalRepository.findByMealIdentityTarget(groupId, normalizedIdentityKey, signalType);
            }
        };
    }

    private String normalizeMealIdentityKey(String mealIdentityKey) {
        if (mealIdentityKey == null || mealIdentityKey.isBlank()) {
            throw new IllegalArgumentException("mealIdentityKey must not be blank");
        }
        return mealIdentityKey.trim();
    }

    private LocalDate localDateForIsoWeek(int year, int isoWeek, int dayOfWeek) {
        return LocalDate.of(year, 1, 4)
                .with(WeekFields.ISO.weekOfWeekBasedYear(), isoWeek)
                .with(WeekFields.ISO.dayOfWeek(), dayOfWeek);
    }

    private RecipeDuplicateAssessment assessDuplicateAttention(UUID groupId, RecipeDraft draft) {
        String normalizedDraftSourceUrl = normalizeComparableUrl(draft.getSource().sourceUrl());
        if (normalizedDraftSourceUrl != null) {
            for (Recipe recipe : recipeRepository.findByGroupId(groupId)) {
                if (normalizedDraftSourceUrl.equals(normalizeComparableUrl(recipe.getSource().sourceUrl()))) {
                    return new RecipeDuplicateAssessment(
                            true,
                            RecipeDuplicateMatchType.EXACT_SOURCE_URL,
                            recipe.getId(),
                            "This recipe link is already saved in your library."
                    );
                }
            }
        }

        String normalizedDraftName = normalizeComparableText(draft.getName());
        String normalizedDraftSourceName = normalizeComparableText(draft.getSource().sourceName());
        if (normalizedDraftName != null && normalizedDraftSourceName != null) {
            for (Recipe recipe : recipeRepository.findByGroupId(groupId)) {
                if (normalizedDraftName.equals(normalizeComparableText(recipe.getName()))
                        && normalizedDraftSourceName.equals(normalizeComparableText(recipe.getSource().sourceName()))) {
                    return new RecipeDuplicateAssessment(
                            true,
                            RecipeDuplicateMatchType.SAME_NAME_AND_SOURCE,
                            recipe.getId(),
                            "A recipe with the same name and source is already in your library."
                    );
                }
            }
        }

        return RecipeDuplicateAssessment.clear();
    }

    private RecipeDraftState determineDraftState(
            RecipeProvenance provenance,
            String name,
            List<Ingredient> ingredients,
            boolean markReady
    ) {
        if (!hasDraftCoreContent(name, ingredients)) {
            return RecipeDraftState.DRAFT_OPEN;
        }
        if (needsDraftReviewByDefault(provenance.originKind()) && !markReady) {
            return RecipeDraftState.DRAFT_NEEDS_REVIEW;
        }
        return RecipeDraftState.DRAFT_READY;
    }

    private boolean needsDraftReviewByDefault(RecipeOriginKind originKind) {
        return originKind == RecipeOriginKind.URL_IMPORT
                || originKind == RecipeOriginKind.PASTED_TEXT
                || originKind == RecipeOriginKind.DOCUMENT_IMPORT
                || originKind == RecipeOriginKind.IMAGE_IMPORT;
    }

    private boolean hasDraftCoreContent(String name, List<Ingredient> ingredients) {
        return normalizeOptionalRecipeText(name) != null
                && ingredients != null
                && !ingredients.isEmpty();
    }

    private String normalizeMealTitle(String mealTitle, Recipe recipe) {
        if (mealTitle != null) {
            String normalized = mealTitle.trim();
            if (!normalized.isEmpty()) {
                return normalized;
            }
        }
        if (recipe != null) {
            return recipe.getName();
        }
        throw new IllegalArgumentException("mealTitle must not be blank");
    }

    private boolean sameMealContent(
            PlannedMeal existingMeal,
            String normalizedMealTitle,
            UUID recipeId,
            String recipeTitleSnapshot
    ) {
        return existingMeal.getMealTitle().equals(normalizedMealTitle)
                && java.util.Objects.equals(existingMeal.getRecipeId(), recipeId)
                && java.util.Objects.equals(existingMeal.getRecipeTitleSnapshot(), recipeTitleSnapshot);
    }

    private String normalizeRecipeName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        String normalized = name.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return normalized;
    }

    private String normalizeOptionalRecipeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeOptionalInstructions(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replace("\r\n", "\n");
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeOptionalRecipeUrl(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private RecipeOriginKind normalizeOriginKind(String value) {
        if (value == null) {
            return RecipeOriginKind.MANUAL;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return RecipeOriginKind.MANUAL;
        }
        return RecipeOriginKind.valueOf(normalized);
    }

    private List<Ingredient> toDomainIngredients(List<IngredientInput> inputs) {
        if (inputs == null) {
            throw new IllegalArgumentException("ingredients must not be null");
        }
        List<Ingredient> ingredients = new ArrayList<>();
        int nextPosition = 1;
        for (IngredientInput input : inputs) {
            if (input == null) {
                throw new IllegalArgumentException("ingredient input must not be null");
            }
            int position = input.position() == null ? nextPosition : input.position();
            nextPosition = position + 1;
            ingredients.add(new Ingredient(
                    UUID.randomUUID(),
                    normalizeRecipeIngredientName(input.name()),
                    normalizeOptionalIngredientRawText(input.rawText()),
                    input.quantity(),
                    input.unit(),
                    position
            ));
        }
        return ingredients;
    }

    private RecipeView toView(Recipe recipe, boolean includeDeleteMetadata) {
        List<IngredientView> ingredients = new ArrayList<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
            ingredients.add(new IngredientView(
                    ingredient.getId(),
                    ingredient.getName(),
                    ingredient.getRawText(),
                    ingredient.getQuantity(),
                    toViewUnit(ingredient.getUnit()),
                    ingredient.getPosition()
            ));
        }
        DeleteEligibility deleteEligibility = includeDeleteMetadata
                ? getDeleteEligibility(recipe.getGroupId(), recipe)
                : new DeleteEligibility(false, null);
        return new RecipeView(
                recipe.getId(),
                recipe.getGroupId(),
                recipe.getName(),
                recipe.getSourceName(),
                recipe.getSourceUrl(),
                recipe.getOriginKind().name(),
                recipe.getServings(),
                recipe.getMakeSoonAt(),
                recipe.getShortNote(),
                recipe.getInstructions(),
                recipe.getCreatedAt(),
                recipe.getUpdatedAt(),
                recipe.getArchivedAt(),
                recipe.isSavedInRecipes(),
                deleteEligibility.eligible(),
                deleteEligibility.blockedReason(),
                ingredients
        );
    }

    private RecipeDraftView toDraftView(RecipeDraft draft) {
        List<IngredientView> ingredients = new ArrayList<>();
        for (Ingredient ingredient : draft.getIngredients()) {
            ingredients.add(new IngredientView(
                    ingredient.getId(),
                    ingredient.getName(),
                    ingredient.getRawText(),
                    ingredient.getQuantity(),
                    toViewUnit(ingredient.getUnit()),
                    ingredient.getPosition()
            ));
        }
        return new RecipeDraftView(
                draft.getId(),
                draft.getGroupId(),
                toDraftStateValue(draft.getState()),
                draft.getName(),
                toSourceView(draft.getSource()),
                toProvenanceView(draft.getProvenance()),
                draft.getServings(),
                draft.getShortNote(),
                draft.getInstructions().body(),
                draft.getCreatedAt(),
                draft.getUpdatedAt(),
                ingredients
        );
    }

    private RecipeDetailView toDetailView(Recipe recipe) {
        List<IngredientView> ingredients = new ArrayList<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
            ingredients.add(new IngredientView(
                    ingredient.getId(),
                    ingredient.getName(),
                    ingredient.getRawText(),
                    ingredient.getQuantity(),
                    toViewUnit(ingredient.getUnit()),
                    ingredient.getPosition()
            ));
        }
        return new RecipeDetailView(
                recipe.getId(),
                recipe.getGroupId(),
                recipe.getName(),
                toSourceView(recipe.getSource()),
                toProvenanceView(recipe.getProvenance()),
                toLifecycleView(recipe),
                recipe.getServings(),
                recipe.getMakeSoonAt(),
                recipe.getShortNote(),
                recipe.getRecipeInstructions().body(),
                recipe.getCreatedAt(),
                recipe.getUpdatedAt(),
                recipe.isSavedInRecipes(),
                ingredients
        );
    }

    private RecipeLibraryItemView toLibraryItemView(Recipe recipe) {
        return new RecipeLibraryItemView(
                recipe.getId(),
                recipe.getName(),
                toSourceView(recipe.getSource()),
                toLifecycleView(recipe),
                recipe.getMakeSoonAt(),
                recipe.getUpdatedAt(),
                recipe.getIngredients().size()
        );
    }

    private RecipeDuplicateAssessmentView toDuplicateAssessmentView(
            UUID groupId,
            RecipeDuplicateAssessment assessment
    ) {
        RecipeIdentitySummaryView matchingRecipe = null;
        if (assessment.matchingRecipeId() != null) {
            Recipe recipe = recipeRepository.findByIdAndGroupId(assessment.matchingRecipeId(), groupId).orElse(null);
            if (recipe != null) {
                matchingRecipe = toIdentitySummaryView(recipe);
            }
        }
        return new RecipeDuplicateAssessmentView(
                assessment.attentionRequired(),
                toMatchTypeValue(assessment.matchType()),
                assessment.reason(),
                matchingRecipe
        );
    }

    private RecipeIdentitySummaryView toIdentitySummaryView(Recipe recipe) {
        return new RecipeIdentitySummaryView(
                recipe.getId(),
                recipe.getName(),
                toSourceView(recipe.getSource()),
                toLifecycleView(recipe)
        );
    }

    private RecipeSourceView toSourceView(RecipeSource source) {
        return new RecipeSourceView(
                source == null ? null : source.sourceName(),
                source == null ? null : source.sourceUrl()
        );
    }

    private RecipeProvenanceView toProvenanceView(RecipeProvenance provenance) {
        return new RecipeProvenanceView(
                provenance == null ? null : toOriginValue(provenance.originKind()),
                provenance == null ? null : provenance.referenceUrl()
        );
    }

    private RecipeLifecycleView toLifecycleView(Recipe recipe) {
        DeleteEligibility deleteEligibility = getDeleteEligibility(recipe.getGroupId(), recipe);
        return new RecipeLifecycleView(
                toLifecycleValue(recipe.getLifecycle()),
                deleteEligibility.eligible(),
                deleteEligibility.blockedReason()
        );
    }

    private List<Recipe> findRecentlyUsedSavedRecipes(UUID groupId) {
        LocalDate today = LocalDate.now(clock);
        int currentYear = today.get(WeekFields.ISO.weekBasedYear());
        int currentIsoWeek = today.get(WeekFields.ISO.weekOfWeekBasedYear());
        int currentDayOfWeek = today.getDayOfWeek().getValue();

        List<UUID> orderedRecipeIds = weekPlanRepository.findRecentRecipeIdsOnOrBefore(
                groupId,
                currentYear,
                currentIsoWeek,
                currentDayOfWeek
        );
        if (orderedRecipeIds.isEmpty()) {
            return List.of();
        }

        Set<UUID> seenRecipeIds = new HashSet<>();
        List<UUID> recentRecipeIds = new ArrayList<>();
        for (UUID recipeId : orderedRecipeIds) {
            if (!seenRecipeIds.add(recipeId)) {
                continue;
            }
            recentRecipeIds.add(recipeId);
        }
        if (recentRecipeIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, Recipe> recipesById = new HashMap<>();
        for (Recipe recipe : recipeRepository.findByGroupIdAndIds(groupId, new HashSet<>(recentRecipeIds))) {
            if (recipe.isArchived() || !recipe.isSavedInRecipes()) {
                continue;
            }
            recipesById.put(recipe.getId(), recipe);
        }

        List<Recipe> recipes = new ArrayList<>();
        for (UUID recipeId : recentRecipeIds) {
            Recipe recipe = recipesById.get(recipeId);
            if (recipe != null) {
                recipes.add(recipe);
                if (recipes.size() >= RECENTLY_USED_RECIPES_LIMIT) {
                    break;
                }
            }
        }
        return recipes;
    }

    private List<Recipe> findRecipesForLibraryState(UUID groupId, String state) {
        String normalizedState = state == null ? "active" : state.trim().toLowerCase(Locale.ROOT);
        return switch (normalizedState) {
            case "active" -> recipeRepository.findActiveByGroupId(groupId);
            case "archived" -> recipeRepository.findArchivedByGroupId(groupId);
            default -> throw new IllegalArgumentException("Unknown recipe library state: " + state);
        };
    }

    private String toDraftStateValue(RecipeDraftState state) {
        return switch (state) {
            case DRAFT_OPEN -> "draft_open";
            case DRAFT_NEEDS_REVIEW -> "draft_needs_review";
            case DRAFT_READY -> "draft_ready";
        };
    }

    private String toMealIdentityKindValue(MealIdentityKind kind) {
        return switch (kind) {
            case RECIPE -> "recipe";
            case TITLE_ONLY -> "title_only";
        };
    }

    private String toPreferenceTargetKindValue(HouseholdPreferenceSignalTargetKind targetKind) {
        return switch (targetKind) {
            case RECIPE -> "recipe";
            case MEAL_IDENTITY -> "meal_identity";
        };
    }

    private String toPreferenceSignalTypeValue(HouseholdPreferenceSignalType signalType) {
        return signalType.name().toLowerCase(Locale.ROOT);
    }

    private String toOriginValue(RecipeOriginKind originKind) {
        if (originKind == null) {
            return null;
        }
        return originKind.name().toLowerCase(Locale.ROOT);
    }

    private String toLifecycleValue(RecipeLifecycle lifecycle) {
        return switch (lifecycle) {
            case ACTIVE -> "active";
            case ARCHIVED -> "archived";
        };
    }

    private String toMatchTypeValue(RecipeDuplicateMatchType matchType) {
        if (matchType == null) {
            return null;
        }
        return switch (matchType) {
            case EXACT_SOURCE_URL -> "exact_source_url";
            case SAME_NAME_AND_SOURCE -> "same_name_and_source";
        };
    }

    private String toIngredientCoverageStateValue(IngredientCoverageState state) {
        return switch (state) {
            case COVERED -> "covered";
            case PARTIALLY_COVERED -> "partially_covered";
            case MISSING -> "missing";
            case UNKNOWN -> "unknown";
        };
    }

    private String toShoppingCoverageStateValue(ShoppingCoverageState state) {
        return switch (state) {
            case NONE -> "none";
            case TO_BUY -> "to_buy";
            case BOUGHT -> "bought";
            case MIXED -> "mixed";
            case UNKNOWN -> "unknown";
        };
    }

    private String toMealReadinessStateValue(MealReadinessState state) {
        return switch (state) {
            case NEEDS_SHOPPING -> "needs_shopping";
            case PARTIALLY_READY -> "partially_ready";
            case READY_FROM_SHOPPING_VIEW -> "ready_from_shopping_view";
            case READINESS_UNCLEAR -> "readiness_unclear";
        };
    }

    private String toShoppingLinkStatusValue(ShoppingLinkStatus status) {
        return switch (status) {
            case NOT_LINKED -> "not_linked";
            case LINKED -> "linked";
            case MISSING_LIST -> "missing_list";
        };
    }

    private DeleteEligibility getDeleteEligibility(UUID groupId, Recipe recipe) {
        if (!recipe.isArchived()) {
            return new DeleteEligibility(false, "Recipe must be archived before you can delete it.");
        }
        int[] currentWeek = currentIsoWeek();
        if (weekPlanRepository.existsCurrentOrFutureMealReferencingRecipe(
                groupId,
                recipe.getId(),
                currentWeek[0],
                currentWeek[1]
        )) {
            return new DeleteEligibility(false, "This recipe is still used in planned meals.");
        }
        return new DeleteEligibility(true, null);
    }

    private int[] currentIsoWeek() {
        LocalDate today = LocalDate.now(clock);
        WeekFields weekFields = WeekFields.ISO;
        return new int[] {
                today.get(weekFields.weekBasedYear()),
                today.get(weekFields.weekOfWeekBasedYear())
        };
    }

    private record DeleteEligibility(boolean eligible, String blockedReason) {
    }

    private IngredientUnitView toViewUnit(IngredientUnit unit) {
        if (unit == null) {
            return null;
        }
        return IngredientUnitView.valueOf(unit.name());
    }

    private void pushIngredientsToShopping(
            UUID groupId,
            UUID actorUserId,
            UUID targetShoppingListId,
            String recipeName,
            List<Ingredient> ingredients,
            List<Integer> selectedIngredientPositions
    ) {
        Set<Integer> selectedPositions = selectedIngredientPositions == null
                ? null
                : new HashSet<>(selectedIngredientPositions);
        // Shopping inserts new items at the top. Reverse recipe order here so the
        // final list preserves the original ingredient order for users.
        for (int index = ingredients.size() - 1; index >= 0; index--) {
            Ingredient ingredient = ingredients.get(index);
            if (selectedPositions != null && !selectedPositions.contains(ingredient.getPosition())) {
                continue;
            }
            ShoppingIngredientProjection projection = projectIngredientForShopping(ingredient);
            mealsShoppingPort.addShoppingItem(
                    groupId,
                    actorUserId,
                    targetShoppingListId,
                    projection.name(),
                    projection.quantity(),
                    projection.unitName(),
                    "meal-plan",
                    normalizeRecipeName(recipeName)
            );
        }
    }

    private ShoppingIngredientProjection projectIngredientForShopping(Ingredient ingredient) {
        ReducedIngredientName reducedName = reduceIngredientNameForShopping(ingredient);
        boolean preserveQuantityDetails = shouldPreserveShoppingQuantityDetails(ingredient, reducedName);
        return new ShoppingIngredientProjection(
                reducedName.name(),
                preserveQuantityDetails ? ingredient.getQuantity() : null,
                preserveQuantityDetails ? mapRecipeUnitToShoppingUnitName(ingredient.getUnit()) : null
        );
    }

    private ReducedIngredientName reduceIngredientNameForShopping(Ingredient ingredient) {
        String candidate = ingredient.getName();
        candidate = stripIgnorableTrailingCommaClauses(candidate);
        candidate = stripKnownShoppingSuffixes(candidate);
        candidate = stripLeadingIngredientModifiers(candidate);

        boolean strippedLeadingMeasureToken = false;
        boolean strippedTrailingCountToken = false;
        if (ingredient.getQuantity() == null || ingredient.getUnit() == null || ingredient.getUnit() == IngredientUnit.PCS) {
            candidate = stripLeadingStandaloneQuantity(candidate);
            StripResult leadingMeasureResult = stripLeadingCookingMeasureTokens(candidate);
            candidate = leadingMeasureResult.value();
            strippedLeadingMeasureToken = leadingMeasureResult.changed();
            StripResult trailingCountResult = stripTrailingCountTokens(candidate);
            candidate = trailingCountResult.value();
            strippedTrailingCountToken = trailingCountResult.changed();
        }
        candidate = stripPreparationSuffixes(candidate);
        if (ingredient.getQuantity() == null || ingredient.getUnit() == null || ingredient.getUnit() == IngredientUnit.PCS) {
            StripResult trailingCountResult = stripTrailingCountTokens(candidate);
            candidate = trailingCountResult.value();
            strippedTrailingCountToken = strippedTrailingCountToken || trailingCountResult.changed();
        }
        candidate = applyLowRiskShoppingCanonicalName(candidate);
        String normalized = normalizeOptionalIngredientRawText(candidate);
        if (normalized == null) {
            normalized = ingredient.getName();
        }
        return new ReducedIngredientName(
                normalizeIngredientName(normalized),
                strippedLeadingMeasureToken,
                strippedTrailingCountToken
        );
    }

    private boolean shouldPreserveShoppingQuantityDetails(Ingredient ingredient, ReducedIngredientName reducedName) {
        if (ingredient.getQuantity() == null || ingredient.getUnit() == null) {
            return false;
        }
        if (ingredient.getUnit() == IngredientUnit.PCS
                && (reducedName.strippedLeadingMeasureToken() || reducedName.strippedTrailingCountToken())) {
            return false;
        }
        return mapRecipeUnitToShoppingUnitName(ingredient.getUnit()) != null;
    }

    private String stripIgnorableTrailingCommaClauses(String value) {
        String current = value;
        while (true) {
            int commaIndex = current.lastIndexOf(',');
            if (commaIndex < 0) {
                return current;
            }
            String trailingClause = current.substring(commaIndex + 1).trim();
            if (!isIgnorableShoppingClause(trailingClause)) {
                return current;
            }
            current = current.substring(0, commaIndex).trim();
        }
    }

    private boolean isIgnorableShoppingClause(String clause) {
        String normalized = normalizeClause(clause);
        if (normalized == null) {
            return false;
        }
        return SHOPPING_CONTEXT_SUFFIXES.contains(normalized)
                || SHOPPING_PREPARATION_SUFFIXES.contains(normalized);
    }

    private String stripKnownShoppingSuffixes(String value) {
        String current = value;
        boolean changed;
        do {
            changed = false;
            String normalized = normalizeClause(current);
            if (normalized == null) {
                return value;
            }
            for (String suffix : SHOPPING_CONTEXT_SUFFIXES) {
                if (normalized.endsWith(suffix)) {
                    current = current.substring(0, current.length() - suffix.length()).trim();
                    changed = true;
                    break;
                }
            }
        } while (changed);
        return current;
    }

    private String stripLeadingIngredientModifiers(String value) {
        String current = value.trim();
        boolean changed;
        do {
            changed = false;
            String normalized = normalizeClause(current);
            if (normalized == null) {
                return value;
            }
            for (String modifier : LEADING_INGREDIENT_MODIFIERS) {
                if (normalized.equals(modifier)) {
                    return value;
                }
                if (normalized.startsWith(modifier + " ")) {
                    current = current.substring(modifier.length()).trim();
                    changed = true;
                    break;
                }
            }
        } while (changed);
        return current;
    }

    private String stripLeadingStandaloneQuantity(String value) {
        String current = value.trim();
        while (true) {
            String updated = LEADING_QUANTITY_PATTERN.matcher(current).replaceFirst("");
            if (updated.equals(current)) {
                return current;
            }
            current = updated.trim();
        }
    }

    private StripResult stripLeadingCookingMeasureTokens(String value) {
        String current = value.trim();
        boolean changed = false;
        do {
            boolean removedInIteration = false;
            String normalized = normalizeClause(current);
            if (normalized == null) {
                return new StripResult(value, false);
            }
            for (String token : LEADING_COOKING_MEASURE_TOKENS) {
                if (normalized.equals(token)) {
                    return new StripResult(value, false);
                }
                if (normalized.startsWith(token + " ")) {
                    current = current.substring(token.length()).trim();
                    if (normalizeClause(current) != null && normalizeClause(current).startsWith("of ")) {
                        current = current.substring(2).trim();
                    }
                    changed = true;
                    removedInIteration = true;
                    break;
                }
            }
            if (!removedInIteration) {
                return new StripResult(current, changed);
            }
        } while (true);
    }

    private StripResult stripTrailingCountTokens(String value) {
        String current = value.trim();
        boolean changed = false;
        while (true) {
            String normalized = normalizeClause(current);
            if (normalized == null) {
                return new StripResult(value, false);
            }
            boolean stripped = false;
            for (String token : TRAILING_COUNT_TOKENS) {
                if (normalized.endsWith(" " + token)) {
                    current = current.substring(0, current.length() - token.length()).trim();
                    changed = true;
                    stripped = true;
                    break;
                }
            }
            if (!stripped) {
                return new StripResult(current, changed);
            }
        }
    }

    private String stripPreparationSuffixes(String value) {
        String current = value;
        boolean changed;
        do {
            changed = false;
            String normalized = normalizeClause(current);
            if (normalized == null) {
                return value;
            }
            for (String suffix : SHOPPING_PREPARATION_SUFFIXES) {
                if (normalized.endsWith(" " + suffix)) {
                    current = current.substring(0, current.length() - suffix.length()).trim();
                    changed = true;
                    break;
                }
            }
        } while (changed);
        return current;
    }

    private String applyLowRiskShoppingCanonicalName(String value) {
        String current = value;
        String normalized = normalizeClause(current);
        if (normalized == null) {
            return value;
        }
        for (Map.Entry<String, String> entry : SHOPPING_CANONICAL_NAME_ENDINGS.entrySet()) {
            if (normalized.endsWith(entry.getKey())) {
                return current.substring(0, current.length() - entry.getKey().length())
                        .concat(entry.getValue())
                        .trim();
            }
        }
        return current;
    }

    private String normalizeClause(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeComparableText(String value) {
        return normalizeClause(value);
    }

    private String normalizeComparableUrl(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeRecentMealKey(String mealTitle, UUID recipeId, String recipeTitleSnapshot) {
        String normalizedMealTitle = normalizeClause(mealTitle);
        String normalizedRecipeTitle = normalizeClause(recipeTitleSnapshot);
        return (normalizedMealTitle == null ? "" : normalizedMealTitle)
                + "|"
                + (recipeId == null ? (normalizedRecipeTitle == null ? "" : normalizedRecipeTitle) : recipeId);
    }

    private String normalizeRecipeIngredientName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("ingredient name must not be null");
        }
        String normalized = name.trim().replaceAll("\\s+", " ");
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("ingredient name must not be blank");
        }
        return normalized;
    }

    private String normalizeOptionalIngredientRawText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replaceAll("\\s+", " ");
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeIngredientName(String name) {
        return normalizeRecipeIngredientName(name).toLowerCase(Locale.ROOT);
    }

    private String mapRecipeUnitToShoppingUnitName(IngredientUnit unit) {
        if (unit == null) {
            return null;
        }
        // Recipes and Shopping intentionally remain separate unit concepts even
        // while the current overlap is still one-to-one.
        return switch (unit) {
            case PCS -> "PCS";
            case PACK -> "PACK";
            case KG -> "KG";
            case HG -> "HG";
            case G -> "G";
            case L -> "L";
            case DL -> "DL";
            case ML -> "ML";
            case TBSP, TSP, KRM -> null;
        };
    }

    private void ensureMealAccess(UUID groupId, UUID actorUserId) {
        try {
            ensureGroupMemberUseCase.execute(groupId, actorUserId);
        } catch (AccessDeniedException ex) {
            throw new MealsAccessDeniedException(ex.getMessage());
        }
    }

    private void validateIsoWeek(int year, int isoWeek) {
        if (isoWeek < 1 || isoWeek > 53) {
            throw new IllegalArgumentException("isoWeek must be between 1 and 53");
        }
        WeekFields weekFields = WeekFields.ISO;
        LocalDate date = LocalDate.of(year, 1, 4)
                .with(weekFields.weekOfWeekBasedYear(), isoWeek)
                .with(weekFields.dayOfWeek(), 1);
        if (date.get(weekFields.weekBasedYear()) != year) {
            throw new IllegalArgumentException("isoWeek is not valid for year");
        }
    }

    private record ShoppingIngredientProjection(String name, java.math.BigDecimal quantity, String unitName) {
    }

    private record ReducedIngredientName(String name, boolean strippedLeadingMeasureToken, boolean strippedTrailingCountToken) {
    }

    private record StripResult(String value, boolean changed) {
    }

}
