package app.lifelinq.features.meals.api;

import app.lifelinq.config.RequestContext;
import app.lifelinq.features.meals.application.MealsApplicationService;
import app.lifelinq.features.meals.contract.AddMealOutput;
import app.lifelinq.features.meals.contract.PlannedMealView;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MealsController {
    private final MealsApplicationService mealsApplicationService;

    public MealsController(MealsApplicationService mealsApplicationService) {
        this.mealsApplicationService = mealsApplicationService;
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
                request.getRecipeTitle(),
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
}
