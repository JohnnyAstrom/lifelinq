package app.lifelinq.features.meals.application;

import app.lifelinq.features.household.contract.EnsureHouseholdMemberUseCase;
import app.lifelinq.features.meals.contract.AddMealOutput;
import app.lifelinq.features.meals.contract.IngredientInput;
import app.lifelinq.features.meals.contract.IngredientView;
import app.lifelinq.features.meals.contract.PlannedMealView;
import app.lifelinq.features.meals.contract.RecipeView;
import app.lifelinq.features.meals.contract.WeekPlanView;
import app.lifelinq.features.meals.domain.Ingredient;
import app.lifelinq.features.meals.domain.MealType;
import app.lifelinq.features.meals.domain.PlannedMeal;
import app.lifelinq.features.meals.domain.Recipe;
import app.lifelinq.features.meals.domain.RecipeRepository;
import app.lifelinq.features.meals.domain.WeekPlan;
import app.lifelinq.features.meals.domain.WeekPlanRepository;
import app.lifelinq.features.shopping.application.ShoppingApplicationService;
import app.lifelinq.features.shopping.contract.ShoppingUnitView;
import app.lifelinq.features.shopping.domain.ShoppingUnit;
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
import org.springframework.transaction.annotation.Transactional;

public class MealsApplicationService {
    private final WeekPlanRepository weekPlanRepository;
    private final RecipeRepository recipeRepository;
    private final EnsureHouseholdMemberUseCase ensureHouseholdMemberUseCase;
    private final ShoppingApplicationService shoppingApplicationService;
    private final Clock clock;

    public MealsApplicationService(
            WeekPlanRepository weekPlanRepository,
            RecipeRepository recipeRepository,
            EnsureHouseholdMemberUseCase ensureHouseholdMemberUseCase,
            ShoppingApplicationService shoppingApplicationService,
            Clock clock
    ) {
        if (weekPlanRepository == null) {
            throw new IllegalArgumentException("weekPlanRepository must not be null");
        }
        if (recipeRepository == null) {
            throw new IllegalArgumentException("recipeRepository must not be null");
        }
        if (ensureHouseholdMemberUseCase == null) {
            throw new IllegalArgumentException("ensureHouseholdMemberUseCase must not be null");
        }
        if (shoppingApplicationService == null) {
            throw new IllegalArgumentException("shoppingApplicationService must not be null");
        }
        if (clock == null) {
            throw new IllegalArgumentException("clock must not be null");
        }
        this.weekPlanRepository = weekPlanRepository;
        this.recipeRepository = recipeRepository;
        this.ensureHouseholdMemberUseCase = ensureHouseholdMemberUseCase;
        this.shoppingApplicationService = shoppingApplicationService;
        this.clock = clock;
    }

    @Transactional
    public RecipeView createRecipe(
            UUID householdId,
            UUID actorUserId,
            String name,
            List<IngredientInput> ingredients
    ) {
        ensureHouseholdMemberUseCase.execute(householdId, actorUserId);
        Recipe recipe = new Recipe(
                UUID.randomUUID(),
                householdId,
                normalizeRecipeName(name),
                clock.instant(),
                toDomainIngredients(ingredients)
        );
        return toView(recipeRepository.save(recipe));
    }

    @Transactional(readOnly = true)
    public RecipeView getRecipe(UUID householdId, UUID actorUserId, UUID recipeId) {
        ensureHouseholdMemberUseCase.execute(householdId, actorUserId);
        return toView(loadRecipe(householdId, recipeId));
    }

    @Transactional(readOnly = true)
    public List<RecipeView> listRecipes(UUID householdId, UUID actorUserId) {
        ensureHouseholdMemberUseCase.execute(householdId, actorUserId);
        List<RecipeView> views = new ArrayList<>();
        for (Recipe recipe : recipeRepository.findByHouseholdId(householdId)) {
            views.add(toView(recipe));
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
            UUID householdId,
            UUID actorUserId,
            UUID recipeId,
            String name,
            List<IngredientInput> ingredients
    ) {
        ensureHouseholdMemberUseCase.execute(householdId, actorUserId);
        Recipe existing = loadRecipe(householdId, recipeId);
        Recipe updated = new Recipe(
                existing.getId(),
                existing.getHouseholdId(),
                normalizeRecipeName(name),
                existing.getCreatedAt(),
                toDomainIngredients(ingredients)
        );
        return toView(recipeRepository.save(updated));
    }

    @Transactional
    public AddMealOutput addOrReplaceMeal(
            UUID householdId,
            UUID actorUserId,
            int year,
            int isoWeek,
            int dayOfWeek,
            MealType mealType,
            UUID recipeId,
            UUID targetShoppingListId
    ) {
        ensureHouseholdMemberUseCase.execute(householdId, actorUserId);
        validateIsoWeek(year, isoWeek);
        Recipe recipe = loadRecipe(householdId, recipeId);
        WeekPlan weekPlan = weekPlanRepository.findByHouseholdAndWeek(householdId, year, isoWeek)
                .orElseGet(() -> new WeekPlan(
                        UUID.randomUUID(),
                        householdId,
                        year,
                        isoWeek,
                        clock.instant()
                ));

        weekPlan.addOrReplaceMeal(dayOfWeek, mealType, recipeId);
        WeekPlan saved = weekPlanRepository.save(weekPlan);

        if (targetShoppingListId != null) {
            // V0.5c intent: recipe ingredients primarily act as shopping-item generators.
            pushIngredientsToShopping(householdId, actorUserId, targetShoppingListId, recipe.getIngredients());
        }

        PlannedMeal savedMeal = saved.getMealOrThrow(dayOfWeek, mealType);
        PlannedMealView mealView = new PlannedMealView(
                savedMeal.getDayOfWeek(),
                savedMeal.getMealType().name(),
                savedMeal.getRecipeId(),
                recipe.getName()
        );
        return new AddMealOutput(saved.getId(), saved.getYear(), saved.getIsoWeek(), mealView);
    }

    @Transactional
    public void removeMeal(
            UUID householdId,
            UUID actorUserId,
            int year,
            int isoWeek,
            int dayOfWeek,
            MealType mealType
    ) {
        ensureHouseholdMemberUseCase.execute(householdId, actorUserId);
        validateIsoWeek(year, isoWeek);
        WeekPlan weekPlan = weekPlanRepository.findByHouseholdAndWeek(householdId, year, isoWeek)
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
            UUID householdId,
            UUID actorUserId,
            int year,
            int isoWeek
    ) {
        ensureHouseholdMemberUseCase.execute(householdId, actorUserId);
        validateIsoWeek(year, isoWeek);
        return weekPlanRepository.findByHouseholdAndWeek(householdId, year, isoWeek)
                .map(this::toView)
                .orElseGet(() -> new WeekPlanView(null, year, isoWeek, null, List.of()));
    }

    private WeekPlanView toView(WeekPlan weekPlan) {
        Set<UUID> recipeIds = new HashSet<>();
        for (PlannedMeal meal : weekPlan.getMeals()) {
            recipeIds.add(meal.getRecipeId());
        }

        Map<UUID, String> namesByRecipeId = new HashMap<>();
        for (Recipe recipe : recipeRepository.findByHouseholdIdAndIds(weekPlan.getHouseholdId(), recipeIds)) {
            namesByRecipeId.put(recipe.getId(), recipe.getName());
        }

        List<PlannedMealView> meals = new ArrayList<>();
        for (PlannedMeal meal : weekPlan.getMeals()) {
            meals.add(new PlannedMealView(
                    meal.getDayOfWeek(),
                    meal.getMealType().name(),
                    meal.getRecipeId(),
                    namesByRecipeId.get(meal.getRecipeId())
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

    private Recipe loadRecipe(UUID householdId, UUID recipeId) {
        if (recipeId == null) {
            throw new IllegalArgumentException("recipeId must not be null");
        }
        return recipeRepository.findByIdAndHouseholdId(recipeId, householdId)
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
                    input.name(),
                    input.quantity(),
                    input.unit(),
                    position
            ));
        }
        return ingredients;
    }

    private RecipeView toView(Recipe recipe) {
        List<IngredientView> ingredients = new ArrayList<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
            ingredients.add(new IngredientView(
                    ingredient.getId(),
                    ingredient.getName(),
                    ingredient.getQuantity(),
                    toViewUnit(ingredient.getUnit()),
                    ingredient.getPosition()
            ));
        }
        return new RecipeView(
                recipe.getId(),
                recipe.getHouseholdId(),
                recipe.getName(),
                recipe.getCreatedAt(),
                ingredients
        );
    }

    private ShoppingUnitView toViewUnit(ShoppingUnit unit) {
        if (unit == null) {
            return null;
        }
        return ShoppingUnitView.valueOf(unit.name());
    }

    private void pushIngredientsToShopping(
            UUID householdId,
            UUID actorUserId,
            UUID targetShoppingListId,
            List<Ingredient> ingredients
    ) {
        for (Ingredient ingredient : ingredients) {
            shoppingApplicationService.addShoppingItem(
                    householdId,
                    actorUserId,
                    targetShoppingListId,
                    normalizeIngredientName(ingredient.getName()),
                    ingredient.getQuantity(),
                    ingredient.getUnit()
            );
        }
    }

    private String normalizeIngredientName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("ingredient name must not be null");
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("ingredient name must not be blank");
        }
        return trimmed.replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
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

}
