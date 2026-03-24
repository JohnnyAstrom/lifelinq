package app.lifelinq.features.meals.domain;

import app.lifelinq.features.meals.contract.MealsShoppingItemSnapshot;
import app.lifelinq.features.meals.contract.MealsShoppingListSnapshot;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class KitchenReadinessEngine {

    public MealShoppingProjection buildMealProjection(
            int year,
            int isoWeek,
            PlannedMeal meal,
            ShoppingLinkReference shoppingLink,
            List<MealIngredientNeed> ingredientNeeds,
            MealsShoppingListSnapshot shoppingListSnapshot
    ) {
        if (meal == null) {
            throw new IllegalArgumentException("meal must not be null");
        }
        if (shoppingLink == null) {
            throw new IllegalArgumentException("shoppingLink must not be null");
        }
        if (ingredientNeeds == null) {
            throw new IllegalArgumentException("ingredientNeeds must not be null");
        }

        List<IngredientCoverage> coverage = ingredientNeeds.stream()
                .map(need -> assessCoverage(need, shoppingListSnapshot))
                .toList();

        ShoppingDelta delta = new ShoppingDelta(coverage.stream()
                .filter(item -> item.state() != IngredientCoverageState.COVERED)
                .toList());

        MealReadinessSignal readiness = deriveReadiness(ingredientNeeds, coverage);

        return new MealShoppingProjection(
                year,
                isoWeek,
                meal.getDayOfWeek(),
                meal.getMealType(),
                meal.getMealTitle(),
                meal.getRecipeId(),
                meal.getRecipeTitleSnapshot(),
                shoppingLink,
                coverage,
                delta,
                readiness
        );
    }

    public WeekShoppingProjection buildWeekProjection(
            UUID weekPlanId,
            int year,
            int isoWeek,
            List<MealShoppingProjection> meals
    ) {
        if (meals == null) {
            throw new IllegalArgumentException("meals must not be null");
        }
        List<IngredientCoverage> unresolved = new ArrayList<>();
        int needsShopping = 0;
        int partiallyReady = 0;
        int ready = 0;
        int unclear = 0;

        for (MealShoppingProjection meal : meals) {
            unresolved.addAll(meal.delta().unresolvedIngredients());
            switch (meal.readiness().state()) {
                case NEEDS_SHOPPING -> needsShopping++;
                case PARTIALLY_READY -> partiallyReady++;
                case READY_FROM_SHOPPING_VIEW -> ready++;
                case READINESS_UNCLEAR -> unclear++;
            }
        }

        return new WeekShoppingProjection(
                weekPlanId,
                year,
                isoWeek,
                needsShopping,
                partiallyReady,
                ready,
                unclear,
                new ShoppingDelta(unresolved),
                meals
        );
    }

    private IngredientCoverage assessCoverage(
            MealIngredientNeed need,
            MealsShoppingListSnapshot shoppingListSnapshot
    ) {
        if (shoppingListSnapshot == null) {
            return new IngredientCoverage(
                    need,
                    IngredientCoverageState.MISSING,
                    ShoppingCoverageState.NONE,
                    0,
                    BigDecimal.ZERO,
                    need.quantity(),
                    null
            );
        }

        List<MealsShoppingItemSnapshot> matches = shoppingListSnapshot.items().stream()
                .filter(item -> need.normalizedShoppingName().equals(item.name()))
                .toList();
        if (matches.isEmpty()) {
            return new IngredientCoverage(
                    need,
                    IngredientCoverageState.MISSING,
                    ShoppingCoverageState.NONE,
                    0,
                    BigDecimal.ZERO,
                    need.quantity(),
                    null
            );
        }

        if (need.quantity() == null || need.unitName() == null) {
            return new IngredientCoverage(
                    need,
                    IngredientCoverageState.COVERED,
                    deriveShoppingState(matches, null),
                    matches.size(),
                    null,
                    null,
                    null
            );
        }

        BigDecimal totalMatchingQuantity = BigDecimal.ZERO;
        BigDecimal boughtMatchingQuantity = BigDecimal.ZERO;
        boolean comparableQuantityFound = false;
        boolean nonComparableQuantityFound = false;

        for (MealsShoppingItemSnapshot match : matches) {
            if (match.quantity() == null || match.unitName() == null) {
                nonComparableQuantityFound = true;
                continue;
            }
            if (!need.unitName().equals(match.unitName())) {
                nonComparableQuantityFound = true;
                continue;
            }
            comparableQuantityFound = true;
            totalMatchingQuantity = totalMatchingQuantity.add(match.quantity());
            if ("BOUGHT".equalsIgnoreCase(match.status())) {
                boughtMatchingQuantity = boughtMatchingQuantity.add(match.quantity());
            }
        }

        if (!comparableQuantityFound) {
            return new IngredientCoverage(
                    need,
                    IngredientCoverageState.UNKNOWN,
                    deriveShoppingState(matches, null),
                    matches.size(),
                    null,
                    null,
                    "Shopping has this ingredient, but not in a comparable quantity."
            );
        }

        IngredientCoverageState state;
        BigDecimal uncoveredQuantity;
        if (totalMatchingQuantity.compareTo(need.quantity()) >= 0) {
            state = IngredientCoverageState.COVERED;
            uncoveredQuantity = BigDecimal.ZERO;
        } else if (totalMatchingQuantity.compareTo(BigDecimal.ZERO) > 0) {
            state = IngredientCoverageState.PARTIALLY_COVERED;
            uncoveredQuantity = need.quantity().subtract(totalMatchingQuantity);
        } else {
            state = IngredientCoverageState.MISSING;
            uncoveredQuantity = need.quantity();
        }

        return new IngredientCoverage(
                need,
                state,
                deriveShoppingState(matches, boughtMatchingQuantity.compareTo(need.quantity()) >= 0),
                matches.size(),
                totalMatchingQuantity,
                uncoveredQuantity,
                nonComparableQuantityFound
                        ? "Some shopping items for this ingredient use a different quantity format."
                        : null
        );
    }

    private MealReadinessSignal deriveReadiness(List<MealIngredientNeed> ingredientNeeds, List<IngredientCoverage> coverage) {
        if (ingredientNeeds.isEmpty()) {
            return new MealReadinessSignal(
                    MealReadinessState.READINESS_UNCLEAR,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0
            );
        }

        int covered = 0;
        int partial = 0;
        int missing = 0;
        int unknown = 0;
        int bought = 0;
        int toBuy = 0;

        for (IngredientCoverage item : coverage) {
            switch (item.state()) {
                case COVERED -> covered++;
                case PARTIALLY_COVERED -> partial++;
                case MISSING -> missing++;
                case UNKNOWN -> unknown++;
            }
            switch (item.shoppingState()) {
                case BOUGHT -> bought++;
                case TO_BUY, MIXED, NONE -> toBuy++;
                case UNKNOWN -> { }
            }
        }

        MealReadinessState state;
        if (unknown > 0 && covered == 0 && partial == 0 && missing == 0) {
            state = MealReadinessState.READINESS_UNCLEAR;
        } else if (missing > 0 && bought == 0) {
            state = MealReadinessState.NEEDS_SHOPPING;
        } else if (missing > 0 || partial > 0 || toBuy > 0) {
            state = bought > 0 ? MealReadinessState.PARTIALLY_READY : MealReadinessState.NEEDS_SHOPPING;
        } else if (covered > 0 && bought == covered) {
            state = MealReadinessState.READY_FROM_SHOPPING_VIEW;
        } else {
            state = MealReadinessState.READINESS_UNCLEAR;
        }

        return new MealReadinessSignal(state, covered, partial, missing, unknown, bought, toBuy);
    }

    private ShoppingCoverageState deriveShoppingState(
            List<MealsShoppingItemSnapshot> matches,
            Boolean fullyBoughtComparableQuantity
    ) {
        long boughtCount = matches.stream()
                .filter(item -> "BOUGHT".equalsIgnoreCase(item.status()))
                .count();
        long toBuyCount = matches.stream()
                .filter(item -> !"BOUGHT".equalsIgnoreCase(item.status()))
                .count();

        if (matches.isEmpty()) {
            return ShoppingCoverageState.NONE;
        }
        if (fullyBoughtComparableQuantity != null) {
            if (Boolean.TRUE.equals(fullyBoughtComparableQuantity)) {
                return ShoppingCoverageState.BOUGHT;
            }
            if (boughtCount > 0 && toBuyCount > 0) {
                return ShoppingCoverageState.MIXED;
            }
            if (toBuyCount > 0) {
                return ShoppingCoverageState.TO_BUY;
            }
            return ShoppingCoverageState.BOUGHT;
        }
        if (boughtCount > 0 && toBuyCount > 0) {
            return ShoppingCoverageState.MIXED;
        }
        if (boughtCount > 0) {
            return ShoppingCoverageState.BOUGHT;
        }
        return ShoppingCoverageState.TO_BUY;
    }
}
