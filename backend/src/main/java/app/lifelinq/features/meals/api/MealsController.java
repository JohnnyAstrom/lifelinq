package app.lifelinq.features.meals.api;

import app.lifelinq.config.RequestContext;
import app.lifelinq.features.meals.application.MealsApplicationService;
import app.lifelinq.features.meals.contract.AddMealOutput;
import app.lifelinq.features.meals.contract.IngredientInput;
import app.lifelinq.features.meals.contract.IngredientView;
import app.lifelinq.features.meals.contract.PlannedMealView;
import app.lifelinq.features.meals.contract.RecipeView;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MealsController {
    private final MealsApplicationService mealsApplicationService;

    public MealsController(MealsApplicationService mealsApplicationService) {
        this.mealsApplicationService = mealsApplicationService;
    }

    @PostMapping("/meals/recipes")
    public ResponseEntity<?> createRecipe(@RequestBody CreateOrUpdateRecipeRequest request) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getHouseholdId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        RecipeView recipe = mealsApplicationService.createRecipe(
                context.getHouseholdId(),
                context.getUserId(),
                request.getName(),
                toIngredientInputs(request.getIngredients())
        );
        return ResponseEntity.ok(toRecipeResponse(recipe));
    }

    @GetMapping("/meals/recipes")
    public ResponseEntity<?> listRecipes() {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getHouseholdId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        List<RecipeResponse> recipes = new ArrayList<>();
        for (RecipeView recipe : mealsApplicationService.listRecipes(context.getHouseholdId(), context.getUserId())) {
            recipes.add(toRecipeResponse(recipe));
        }
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/meals/recipes/{recipeId}")
    public ResponseEntity<?> getRecipe(@PathVariable UUID recipeId) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getHouseholdId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        RecipeView recipe = mealsApplicationService.getRecipe(context.getHouseholdId(), context.getUserId(), recipeId);
        return ResponseEntity.ok(toRecipeResponse(recipe));
    }

    @PutMapping("/meals/recipes/{recipeId}")
    public ResponseEntity<?> updateRecipe(
            @PathVariable UUID recipeId,
            @RequestBody CreateOrUpdateRecipeRequest request
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getHouseholdId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        RecipeView recipe = mealsApplicationService.updateRecipe(
                context.getHouseholdId(),
                context.getUserId(),
                recipeId,
                request.getName(),
                toIngredientInputs(request.getIngredients())
        );
        return ResponseEntity.ok(toRecipeResponse(recipe));
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
        if (context == null || context.getHouseholdId() == null) {
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
                context.getHouseholdId(),
                context.getUserId(),
                year,
                isoWeek,
                dayOfWeek,
                app.lifelinq.features.meals.domain.MealType.valueOf(resolvedMealType),
                request.getRecipeId(),
                request.getTargetShoppingListId()
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
        if (context == null || context.getHouseholdId() == null) {
            return ApiScoping.missingContext();
        }
        if (context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        mealsApplicationService.removeMeal(
                context.getHouseholdId(),
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
        if (context == null || context.getHouseholdId() == null) {
            return ApiScoping.missingContext();
        }
        if (context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        WeekPlanView view = mealsApplicationService.getWeekPlan(
                context.getHouseholdId(),
                context.getUserId(),
                year,
                isoWeek
        );
        return ResponseEntity.ok(toWeekPlanResponse(view));
    }

    private AddMealResponse toAddMealResponse(AddMealOutput output) {
        PlannedMealView meal = output.meal();
        return new AddMealResponse(
                output.weekPlanId(),
                output.year(),
                output.isoWeek(),
                new PlannedMealResponse(meal.dayOfWeek(), meal.mealType(), meal.recipeId(), meal.recipeTitle())
        );
    }

    private WeekPlanResponse toWeekPlanResponse(WeekPlanView view) {
        List<PlannedMealResponse> meals = new ArrayList<>();
        for (PlannedMealView meal : view.meals()) {
            meals.add(new PlannedMealResponse(
                    meal.dayOfWeek(),
                    meal.mealType(),
                    meal.recipeId(),
                    meal.recipeTitle()
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
                    ingredient.quantity(),
                    ingredient.unit(),
                    ingredient.position()
            ));
        }
        return new RecipeResponse(
                view.recipeId(),
                view.householdId(),
                view.name(),
                view.createdAt(),
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
                    request.getQuantity(),
                    request.getUnit(),
                    request.getPosition()
            ));
        }
        return inputs;
    }
}
