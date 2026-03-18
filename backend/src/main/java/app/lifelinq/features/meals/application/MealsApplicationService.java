package app.lifelinq.features.meals.application;

import app.lifelinq.features.group.contract.AccessDeniedException;
import app.lifelinq.features.group.contract.EnsureGroupMemberUseCase;
import app.lifelinq.features.meals.contract.AddMealOutput;
import app.lifelinq.features.meals.contract.IngredientInput;
import app.lifelinq.features.meals.contract.IngredientUnitView;
import app.lifelinq.features.meals.contract.IngredientView;
import app.lifelinq.features.meals.contract.MealsShoppingPort;
import app.lifelinq.features.meals.contract.PlannedMealView;
import app.lifelinq.features.meals.contract.RecipeView;
import app.lifelinq.features.meals.contract.WeekPlanView;
import app.lifelinq.features.meals.domain.Ingredient;
import app.lifelinq.features.meals.domain.IngredientUnit;
import app.lifelinq.features.meals.domain.MealType;
import app.lifelinq.features.meals.domain.PlannedMeal;
import app.lifelinq.features.meals.domain.Recipe;
import app.lifelinq.features.meals.domain.RecipeOriginKind;
import app.lifelinq.features.meals.domain.RecipeRepository;
import app.lifelinq.features.meals.domain.WeekPlan;
import app.lifelinq.features.meals.domain.WeekPlanRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
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
    private final EnsureGroupMemberUseCase ensureGroupMemberUseCase;
    private final MealsShoppingPort mealsShoppingPort;
    private final Clock clock;

    public MealsApplicationService(
            WeekPlanRepository weekPlanRepository,
            RecipeRepository recipeRepository,
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
        this.ensureGroupMemberUseCase = ensureGroupMemberUseCase;
        this.mealsShoppingPort = mealsShoppingPort;
        this.clock = clock;
    }

    @Transactional
    public RecipeView createRecipe(
            UUID groupId,
            UUID actorUserId,
            String name,
            String sourceName,
            String sourceUrl,
            String originKind,
            String shortNote,
            String instructions,
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
                normalizeOptionalRecipeText(shortNote),
                normalizeOptionalInstructions(instructions),
                now,
                now,
                null,
                toDomainIngredients(ingredients)
        );
        return toView(recipeRepository.save(recipe), false);
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

    @Transactional
    public RecipeView updateRecipe(
            UUID groupId,
            UUID actorUserId,
            UUID recipeId,
            String name,
            String sourceName,
            String sourceUrl,
            String originKind,
            String shortNote,
            String instructions,
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
                normalizeOptionalRecipeText(shortNote),
                normalizeOptionalInstructions(instructions),
                existing.getCreatedAt(),
                clock.instant(),
                existing.getArchivedAt(),
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
                existing.getShortNote(),
                existing.getInstructions(),
                existing.getCreatedAt(),
                now,
                now,
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
                existing.getShortNote(),
                existing.getInstructions(),
                existing.getCreatedAt(),
                now,
                null,
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
            UUID recipeId,
            UUID targetShoppingListId,
            List<Integer> selectedIngredientPositions
    ) {
        ensureMealAccess(groupId, actorUserId);
        validateIsoWeek(year, isoWeek);
        Recipe recipe = loadRecipe(groupId, recipeId);
        WeekPlan weekPlan = weekPlanRepository.findByGroupAndWeek(groupId, year, isoWeek)
                .orElseGet(() -> new WeekPlan(
                        UUID.randomUUID(),
                        groupId,
                        year,
                        isoWeek,
                        clock.instant()
                ));

        weekPlan.addOrReplaceMeal(dayOfWeek, mealType, recipeId, recipe.getName());
        WeekPlan saved = weekPlanRepository.save(weekPlan);

        if (targetShoppingListId != null) {
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
                savedMeal.getRecipeTitleSnapshot()
        );
        return new AddMealOutput(saved.getId(), saved.getYear(), saved.getIsoWeek(), mealView);
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
                .orElseGet(() -> new WeekPlanView(null, year, isoWeek, null, List.of()));
    }

    private WeekPlanView toView(WeekPlan weekPlan) {
        Set<UUID> recipeIds = new HashSet<>();
        for (PlannedMeal meal : weekPlan.getMeals()) {
            recipeIds.add(meal.getRecipeId());
        }

        Map<UUID, String> namesByRecipeId = new HashMap<>();
        for (Recipe recipe : recipeRepository.findByGroupIdAndIds(weekPlan.getGroupId(), recipeIds)) {
            namesByRecipeId.put(recipe.getId(), recipe.getName());
        }

        List<PlannedMealView> meals = new ArrayList<>();
        for (PlannedMeal meal : weekPlan.getMeals()) {
            meals.add(new PlannedMealView(
                    meal.getDayOfWeek(),
                    meal.getMealType().name(),
                    meal.getRecipeId(),
                    namesByRecipeId.getOrDefault(meal.getRecipeId(), meal.getRecipeTitleSnapshot())
            ));
        }
        return new WeekPlanView(
                weekPlan.getId(),
                weekPlan.getYear(),
                weekPlan.getIsoWeek(),
                weekPlan.getCreatedAt(),
                meals
        );
    }

    private Recipe loadRecipe(UUID groupId, UUID recipeId) {
        if (recipeId == null) {
            throw new IllegalArgumentException("recipeId must not be null");
        }
        return recipeRepository.findByIdAndGroupId(recipeId, groupId)
                .orElseThrow(() -> new RecipeNotFoundException(recipeId));
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
                recipe.getShortNote(),
                recipe.getInstructions(),
                recipe.getCreatedAt(),
                recipe.getUpdatedAt(),
                recipe.getArchivedAt(),
                deleteEligibility.eligible(),
                deleteEligibility.blockedReason(),
                ingredients
        );
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
