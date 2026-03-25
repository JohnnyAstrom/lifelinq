package app.lifelinq.features.meals.domain;

import app.lifelinq.features.meals.contract.MealsShoppingItemSnapshot;
import app.lifelinq.features.meals.contract.MealsShoppingListSnapshot;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class WeekShoppingReviewEngine {

    public WeekShoppingReview buildWeekReview(
            UUID weekPlanId,
            int year,
            int isoWeek,
            WeekShoppingReviewLink reviewLink,
            List<WeekIngredientOccurrence> occurrences,
            MealsShoppingListSnapshot shoppingListSnapshot
    ) {
        if (occurrences == null) {
            throw new IllegalArgumentException("occurrences must not be null");
        }

        List<WeekIngredientOccurrence> sortedOccurrences = new ArrayList<>(occurrences);
        sortedOccurrences.sort((left, right) -> {
            int dayCompare = Integer.compare(left.dayOfWeek(), right.dayOfWeek());
            if (dayCompare != 0) {
                return dayCompare;
            }
            int mealTypeCompare = Integer.compare(left.mealType().ordinal(), right.mealType().ordinal());
            if (mealTypeCompare != 0) {
                return mealTypeCompare;
            }
            int positionCompare = Integer.compare(left.need().position(), right.need().position());
            if (positionCompare != 0) {
                return positionCompare;
            }
            return left.need().ingredientName().compareToIgnoreCase(right.need().ingredientName());
        });

        Map<AggregationKey, List<WeekIngredientOccurrence>> grouped = new LinkedHashMap<>();
        for (WeekIngredientOccurrence occurrence : sortedOccurrences) {
            grouped.computeIfAbsent(toAggregationKey(occurrence.need()), ignored -> new ArrayList<>())
                    .add(occurrence);
        }

        List<AggregatedIngredientComparison> ingredients = new ArrayList<>();
        for (List<WeekIngredientOccurrence> group : grouped.values()) {
            ingredients.add(compare(aggregate(group), shoppingListSnapshot));
        }

        return new WeekShoppingReview(
                weekPlanId,
                year,
                isoWeek,
                shoppingListSnapshot == null ? null : shoppingListSnapshot.listId(),
                reviewLink,
                ingredients
        );
    }

    private AggregationKey toAggregationKey(MealIngredientNeed need) {
        if (need.quantity() == null || need.unitName() == null) {
            return new AggregationKey(need.normalizedShoppingName(), "unquantified", null);
        }
        return new AggregationKey(need.normalizedShoppingName(), "quantified", need.unitName());
    }

    private AggregatedIngredientNeed aggregate(List<WeekIngredientOccurrence> group) {
        WeekIngredientOccurrence first = group.get(0);
        BigDecimal totalQuantity = first.need().quantity() == null
                ? null
                : group.stream()
                        .map(occurrence -> occurrence.need().quantity())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        LinkedHashSet<WeekShoppingContributorMeal> contributors = new LinkedHashSet<>();
        List<String> lineKeyParts = new ArrayList<>();
        for (WeekIngredientOccurrence occurrence : group) {
            contributors.add(new WeekShoppingContributorMeal(
                    occurrence.dayOfWeek(),
                    occurrence.mealType(),
                    occurrence.mealTitle()
            ));
            lineKeyParts.add(
                    occurrence.dayOfWeek()
                            + ":" + occurrence.mealType().name()
                            + ":" + occurrence.need().position()
                            + ":" + occurrence.need().ingredientId()
            );
        }
        lineKeyParts.sort(String::compareTo);
        String lineKey = first.need().normalizedShoppingName() + "|" + String.join("|", lineKeyParts);
        String lineId = UUID.nameUUIDFromBytes(lineKey.getBytes(StandardCharsets.UTF_8)).toString();

        return new AggregatedIngredientNeed(
                lineId,
                first.need().ingredientName(),
                first.need().normalizedShoppingName(),
                totalQuantity,
                first.need().unitName(),
                List.copyOf(contributors)
        );
    }

    private AggregatedIngredientComparison compare(
            AggregatedIngredientNeed need,
            MealsShoppingListSnapshot shoppingListSnapshot
    ) {
        if (shoppingListSnapshot == null) {
            return new AggregatedIngredientComparison(
                    need,
                    AggregatedIngredientComparisonState.ADD_TO_LIST,
                    null,
                    need.totalQuantity()
            );
        }

        List<MealsShoppingItemSnapshot> matches = shoppingListSnapshot.items().stream()
                .filter(item -> need.normalizedShoppingName().equals(item.name()))
                .toList();
        if (matches.isEmpty()) {
            return new AggregatedIngredientComparison(
                    need,
                    AggregatedIngredientComparisonState.ADD_TO_LIST,
                    BigDecimal.ZERO,
                    need.totalQuantity()
            );
        }

        if (need.totalQuantity() == null || need.unitName() == null) {
            return new AggregatedIngredientComparison(
                    need,
                    AggregatedIngredientComparisonState.ALREADY_ON_LIST,
                    null,
                    null
            );
        }

        BigDecimal comparableQuantity = BigDecimal.ZERO;
        for (MealsShoppingItemSnapshot match : matches) {
            if (match.quantity() == null || match.unitName() == null) {
                continue;
            }
            if (!need.unitName().equals(match.unitName())) {
                continue;
            }
            comparableQuantity = comparableQuantity.add(match.quantity());
        }

        if (comparableQuantity.compareTo(need.totalQuantity()) >= 0) {
            return new AggregatedIngredientComparison(
                    need,
                    AggregatedIngredientComparisonState.ALREADY_ON_LIST,
                    comparableQuantity,
                    null
            );
        }

        return new AggregatedIngredientComparison(
                need,
                AggregatedIngredientComparisonState.ADD_TO_LIST,
                comparableQuantity,
                need.totalQuantity().subtract(comparableQuantity)
        );
    }

    public record WeekIngredientOccurrence(
            int dayOfWeek,
            MealType mealType,
            String mealTitle,
            MealIngredientNeed need
    ) {
        public WeekIngredientOccurrence {
            if (dayOfWeek < 1 || dayOfWeek > 7) {
                throw new IllegalArgumentException("dayOfWeek must be between 1 and 7");
            }
            if (mealType == null) {
                throw new IllegalArgumentException("mealType must not be null");
            }
            if (mealTitle == null || mealTitle.isBlank()) {
                throw new IllegalArgumentException("mealTitle must not be blank");
            }
            if (need == null) {
                throw new IllegalArgumentException("need must not be null");
            }
        }
    }

    private record AggregationKey(
            String normalizedShoppingName,
            String mode,
            String unitName
    ) {
    }
}
