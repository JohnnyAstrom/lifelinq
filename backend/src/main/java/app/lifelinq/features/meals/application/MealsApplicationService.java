package app.lifelinq.features.meals.application;

import app.lifelinq.features.household.contract.EnsureHouseholdMemberUseCase;
import app.lifelinq.features.meals.contract.AddMealOutput;
import app.lifelinq.features.meals.contract.PlannedMealView;
import app.lifelinq.features.meals.contract.WeekPlanView;
import app.lifelinq.features.meals.domain.PlannedMeal;
import app.lifelinq.features.meals.domain.RecipeRef;
import app.lifelinq.features.meals.domain.WeekPlan;
import app.lifelinq.features.meals.domain.WeekPlanRepository;
import app.lifelinq.features.shopping.application.ShoppingApplicationService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

public class MealsApplicationService {
    private final WeekPlanRepository weekPlanRepository;
    private final EnsureHouseholdMemberUseCase ensureHouseholdMemberUseCase;
    private final ShoppingApplicationService shoppingApplicationService;
    private final Clock clock;

    public MealsApplicationService(
            WeekPlanRepository weekPlanRepository,
            EnsureHouseholdMemberUseCase ensureHouseholdMemberUseCase,
            ShoppingApplicationService shoppingApplicationService,
            Clock clock
    ) {
        if (weekPlanRepository == null) {
            throw new IllegalArgumentException("weekPlanRepository must not be null");
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
        this.ensureHouseholdMemberUseCase = ensureHouseholdMemberUseCase;
        this.shoppingApplicationService = shoppingApplicationService;
        this.clock = clock;
    }

    @Transactional
    public AddMealOutput addOrReplaceMeal(
            UUID householdId,
            UUID actorUserId,
            int year,
            int isoWeek,
            int dayOfWeek,
            UUID recipeId,
            String recipeTitle,
            UUID targetShoppingListId
    ) {
        ensureHouseholdMemberUseCase.execute(householdId, actorUserId);
        validateIsoWeek(year, isoWeek);
        WeekPlan weekPlan = weekPlanRepository.findByHouseholdAndWeek(householdId, year, isoWeek)
                .orElseGet(() -> new WeekPlan(
                        UUID.randomUUID(),
                        householdId,
                        year,
                        isoWeek,
                        clock.instant()
                ));

        RecipeRef recipeRef = new RecipeRef(recipeId, recipeTitle);
        weekPlan.addOrReplaceMeal(dayOfWeek, recipeRef);
        WeekPlan saved = saveWithRetryOnWeekPlanConflict(weekPlan, householdId, year, isoWeek, dayOfWeek);

        if (targetShoppingListId != null) {
            // ShoppingApplicationService will verify list ownership.
            shoppingApplicationService.addShoppingItem(
                    householdId,
                    actorUserId,
                    targetShoppingListId,
                    recipeTitle
            );
        }

        PlannedMeal savedMeal = saved.getMealOrThrow(dayOfWeek);
        PlannedMealView mealView = new PlannedMealView(
                savedMeal.getDayOfWeek(),
                savedMeal.getRecipeRef().recipeId(),
                savedMeal.getRecipeRef().title()
        );
        return new AddMealOutput(saved.getId(), saved.getYear(), saved.getIsoWeek(), mealView);
    }

    private WeekPlan saveWithRetryOnWeekPlanConflict(
            WeekPlan weekPlan,
            UUID householdId,
            int year,
            int isoWeek,
            int dayOfWeek
    ) {
        try {
            return weekPlanRepository.save(weekPlan);
        } catch (DataIntegrityViolationException ex) {
            WeekPlan existing = weekPlanRepository.findByHouseholdAndWeek(householdId, year, isoWeek)
                    .orElseThrow(() -> ex);
            existing.addOrReplaceMeal(dayOfWeek, weekPlan.getMealOrThrow(dayOfWeek).getRecipeRef());
            return weekPlanRepository.save(existing);
        }
    }

    @Transactional
    public void removeMeal(
            UUID householdId,
            UUID actorUserId,
            int year,
            int isoWeek,
            int dayOfWeek
    ) {
        ensureHouseholdMemberUseCase.execute(householdId, actorUserId);
        validateIsoWeek(year, isoWeek);
        WeekPlan weekPlan = weekPlanRepository.findByHouseholdAndWeek(householdId, year, isoWeek)
                .orElseThrow(() -> new MealNotFoundException("Meal not found"));
        try {
            weekPlan.removeMeal(dayOfWeek);
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
        List<PlannedMealView> meals = new ArrayList<>();
        for (PlannedMeal meal : weekPlan.getMeals()) {
            meals.add(new PlannedMealView(
                    meal.getDayOfWeek(),
                    meal.getRecipeRef().recipeId(),
                    meal.getRecipeRef().title()
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
