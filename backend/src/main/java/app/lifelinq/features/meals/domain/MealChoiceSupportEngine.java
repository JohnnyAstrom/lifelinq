package app.lifelinq.features.meals.domain;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class MealChoiceSupportEngine {
    private static final int RECENT_WINDOW_DAYS = 21;
    private static final int RECENT_USAGE_WINDOW_DAYS = 56;
    private static final int MAX_RECENT_CANDIDATES = 6;
    private static final int MAX_FAMILIAR_CANDIDATES = 8;
    private static final int MAX_FALLBACK_CANDIDATES = 5;
    private static final int MAX_MAKE_SOON_CANDIDATES = 6;

    public List<MealUsageAggregate> summarizeMealUsage(
            List<MealOccurrence> occurrences,
            Collection<Recipe> recipes,
            List<HouseholdPreferenceSignal> preferenceSignals,
            LocalDate referenceDate
    ) {
        Map<UUID, Recipe> recipesById = indexRecipes(recipes);
        PreferenceIndex preferenceIndex = PreferenceIndex.from(preferenceSignals);
        Map<String, AggregateAccumulator> byIdentity = new LinkedHashMap<>();
        for (MealOccurrence occurrence : occurrences) {
            MealIdentity identity = resolveIdentity(occurrence, recipesById);
            byIdentity.computeIfAbsent(identity.key(), ignored -> new AggregateAccumulator(identity))
                    .add(occurrence);
        }

        List<MealUsageAggregate> aggregates = new ArrayList<>();
        for (AggregateAccumulator accumulator : byIdentity.values()) {
            PreferenceResolution preferenceResolution = preferenceIndex.resolve(accumulator.identity());
            Recipe recipe = accumulator.identity().recipeId() == null
                    ? null
                    : recipesById.get(accumulator.identity().recipeId());
            aggregates.add(accumulator.toAggregate(referenceDate, recipe, preferenceResolution));
        }
        aggregates.sort(candidateAggregateComparator(referenceDate, null));
        return aggregates;
    }

    public List<RecipeUsageHistory> summarizeRecipeUsage(
            List<MealOccurrence> occurrences,
            Collection<Recipe> recipes,
            List<HouseholdPreferenceSignal> preferenceSignals,
            LocalDate referenceDate
    ) {
        Map<UUID, Recipe> recipesById = indexRecipes(recipes);
        PreferenceIndex preferenceIndex = PreferenceIndex.from(preferenceSignals);
        Map<UUID, RecipeAccumulator> byRecipeId = new LinkedHashMap<>();
        for (MealOccurrence occurrence : occurrences) {
            if (occurrence.recipeId() == null) {
                continue;
            }
            byRecipeId.computeIfAbsent(
                    occurrence.recipeId(),
                    recipeId -> new RecipeAccumulator(recipeId, resolveRecipeTitle(occurrence, recipesById.get(recipeId)))
            ).add(occurrence);
        }

        List<RecipeUsageHistory> result = new ArrayList<>();
        for (RecipeAccumulator accumulator : byRecipeId.values()) {
            Recipe recipe = recipesById.get(accumulator.recipeId());
            PreferenceResolution preferenceResolution = preferenceIndex.resolve(
                    MealIdentity.forRecipe(accumulator.recipeId(), accumulator.recipeTitle())
            );
            result.add(accumulator.toHistory(referenceDate, recipe, preferenceResolution));
        }
        result.sort(recipeUsageComparator(referenceDate));
        return result;
    }

    public PlanningChoiceSupport buildPlanningChoiceSupport(
            PlanningContext context,
            List<MealOccurrence> occurrences,
            Collection<Recipe> recipes,
            List<HouseholdPreferenceSignal> preferenceSignals
    ) {
        LocalDate referenceDate = context.referenceDate();
        Map<UUID, Recipe> recipesById = indexRecipes(recipes);
        List<MealUsageAggregate> aggregates = summarizeMealUsage(
                occurrences,
                recipesById.values(),
                preferenceSignals,
                referenceDate
        );
        Map<String, MealUsageAggregate> aggregateByIdentityKey = new HashMap<>();
        for (MealUsageAggregate aggregate : aggregates) {
            aggregateByIdentityKey.put(aggregate.identity().key(), aggregate);
        }

        List<ReuseCandidate> recentCandidates = aggregates.stream()
                .filter(MealUsageAggregate::recent)
                .limit(MAX_RECENT_CANDIDATES)
                .map(aggregate -> toRecentCandidate(aggregate, context))
                .toList();

        List<ReuseCandidate> familiarCandidates = aggregates.stream()
                .filter(aggregate -> aggregate.familiar() || aggregate.frequent() || aggregate.preferenceFit() || slotFit(aggregate, context))
                .sorted(candidateAggregateComparator(referenceDate, context))
                .limit(MAX_FAMILIAR_CANDIDATES)
                .map(aggregate -> toFamiliarCandidate(aggregate, context))
                .toList();

        List<ReuseCandidate> fallbackCandidates = aggregates.stream()
                .filter(MealUsageAggregate::fallback)
                .sorted(candidateAggregateComparator(referenceDate, context))
                .limit(MAX_FALLBACK_CANDIDATES)
                .map(aggregate -> toFallbackCandidate(aggregate, context))
                .toList();

        List<ReuseCandidate> makeSoonCandidates = recipes.stream()
                .filter(recipe -> !recipe.isArchived())
                .filter(recipe -> recipe.getMakeSoonAt() != null)
                .sorted(Comparator.comparing(Recipe::getMakeSoonAt).reversed())
                .map(recipe -> toMakeSoonCandidate(
                        recipe,
                        aggregateByIdentityKey.get(MealIdentity.recipeKey(recipe.getId())),
                        context
                ))
                .limit(MAX_MAKE_SOON_CANDIDATES)
                .toList();

        return new PlanningChoiceSupport(
                context,
                recentCandidates,
                familiarCandidates,
                fallbackCandidates,
                makeSoonCandidates
        );
    }

    private Map<UUID, Recipe> indexRecipes(Collection<Recipe> recipes) {
        Map<UUID, Recipe> result = new HashMap<>();
        for (Recipe recipe : recipes) {
            result.put(recipe.getId(), recipe);
        }
        return result;
    }

    private MealIdentity resolveIdentity(MealOccurrence occurrence, Map<UUID, Recipe> recipesById) {
        if (occurrence.recipeId() != null) {
            return MealIdentity.forRecipe(
                    occurrence.recipeId(),
                    resolveRecipeTitle(occurrence, recipesById.get(occurrence.recipeId()))
            );
        }
        return MealIdentity.forTitle(occurrence.mealTitle());
    }

    private String resolveRecipeTitle(MealOccurrence occurrence, Recipe recipe) {
        if (recipe != null) {
            return recipe.getName();
        }
        if (occurrence.recipeTitleSnapshot() != null && !occurrence.recipeTitleSnapshot().isBlank()) {
            return occurrence.recipeTitleSnapshot();
        }
        return occurrence.mealTitle();
    }

    private Comparator<MealUsageAggregate> candidateAggregateComparator(LocalDate referenceDate, PlanningContext context) {
        return Comparator
                .comparingInt((MealUsageAggregate aggregate) -> scoreAggregate(aggregate, referenceDate, context))
                .reversed()
                .thenComparing(MealUsageAggregate::lastPlannedDate, Comparator.reverseOrder())
                .thenComparing(Comparator.comparingInt(MealUsageAggregate::totalOccurrences).reversed())
                .thenComparing(aggregate -> aggregate.identity().title().toLowerCase(Locale.ROOT));
    }

    private int scoreAggregate(MealUsageAggregate aggregate, LocalDate referenceDate, PlanningContext context) {
        int score = 0;
        if (aggregate.preferenceFit()) {
            score += 6;
        }
        if (aggregate.makeSoon()) {
            score += 5;
        }
        if (aggregate.fallback()) {
            score += 4;
        }
        if (aggregate.familiar()) {
            score += 4;
        }
        if (aggregate.frequent()) {
            score += 2;
        }
        if (aggregate.recent()) {
            score += 3;
        }
        if (slotFit(aggregate, context)) {
            score += 3;
        }
        score += Math.min(aggregate.totalOccurrences(), 6);
        if (aggregate.deprioritized()) {
            score -= 5;
        }
        long daysSinceLastUse = ChronoUnit.DAYS.between(aggregate.lastPlannedDate(), referenceDate);
        if (daysSinceLastUse <= 7) {
            score += 2;
        }
        return score;
    }

    private Comparator<RecipeUsageHistory> recipeUsageComparator(LocalDate referenceDate) {
        return Comparator
                .comparingInt((RecipeUsageHistory history) -> {
                    int score = 0;
                    if (history.preferenceFit()) {
                        score += 4;
                    }
                    if (history.makeSoon()) {
                        score += 3;
                    }
                    if (history.familiar()) {
                        score += 3;
                    }
                    if (history.frequent()) {
                        score += 2;
                    }
                    score += Math.min(history.totalUses(), 6);
                    if (history.deprioritized()) {
                        score -= 4;
                    }
                    long daysSinceLastUse = ChronoUnit.DAYS.between(history.lastUsedDate(), referenceDate);
                    if (daysSinceLastUse <= 7) {
                        score += 2;
                    }
                    return score;
                })
                .reversed()
                .thenComparing(RecipeUsageHistory::lastUsedDate, Comparator.reverseOrder())
                .thenComparing(Comparator.comparingInt(RecipeUsageHistory::totalUses).reversed())
                .thenComparing(history -> history.recipeTitle().toLowerCase(Locale.ROOT));
    }

    private ReuseCandidate toRecentCandidate(MealUsageAggregate aggregate, PlanningContext context) {
        return new ReuseCandidate(
                ReuseCandidateFamily.RECENT,
                aggregate.identity(),
                aggregate.lastPlannedDate(),
                aggregate.totalOccurrences(),
                aggregate.recent(),
                aggregate.frequent(),
                aggregate.familiar(),
                aggregate.fallback(),
                slotFit(aggregate, context),
                aggregate.preferenceFit(),
                aggregate.deprioritized(),
                aggregate.makeSoon(),
                "planned recently"
        );
    }

    private ReuseCandidate toFamiliarCandidate(MealUsageAggregate aggregate, PlanningContext context) {
        String surfacedBecause = aggregate.preferenceFit()
                ? "fits what your household likes"
                : slotFit(aggregate, context)
                ? "fits this meal slot"
                : aggregate.familiar()
                ? "cooked across multiple weeks"
                : "comes up often in your household";
        return new ReuseCandidate(
                ReuseCandidateFamily.FAMILIAR,
                aggregate.identity(),
                aggregate.lastPlannedDate(),
                aggregate.totalOccurrences(),
                aggregate.recent(),
                aggregate.frequent(),
                aggregate.familiar(),
                aggregate.fallback(),
                slotFit(aggregate, context),
                aggregate.preferenceFit(),
                aggregate.deprioritized(),
                aggregate.makeSoon(),
                surfacedBecause
        );
    }

    private ReuseCandidate toFallbackCandidate(MealUsageAggregate aggregate, PlanningContext context) {
        return new ReuseCandidate(
                ReuseCandidateFamily.FALLBACK,
                aggregate.identity(),
                aggregate.lastPlannedDate(),
                aggregate.totalOccurrences(),
                aggregate.recent(),
                aggregate.frequent(),
                aggregate.familiar(),
                aggregate.fallback(),
                slotFit(aggregate, context),
                aggregate.preferenceFit(),
                aggregate.deprioritized(),
                aggregate.makeSoon(),
                aggregate.preferenceFit() ? "saved as a strong fallback" : "often works as an easy repeat"
        );
    }

    private ReuseCandidate toMakeSoonCandidate(Recipe recipe, MealUsageAggregate aggregate, PlanningContext context) {
        MealIdentity identity = MealIdentity.forRecipe(recipe.getId(), recipe.getName());
        return new ReuseCandidate(
                ReuseCandidateFamily.MAKE_SOON,
                identity,
                aggregate == null ? LocalDate.ofInstant(recipe.getMakeSoonAt(), ZoneOffset.UTC) : aggregate.lastPlannedDate(),
                aggregate == null ? 0 : aggregate.totalOccurrences(),
                aggregate != null && aggregate.recent(),
                aggregate != null && aggregate.frequent(),
                aggregate != null && aggregate.familiar(),
                aggregate != null && aggregate.fallback(),
                aggregate != null && slotFit(aggregate, context),
                aggregate != null && aggregate.preferenceFit(),
                aggregate != null && aggregate.deprioritized(),
                true,
                "marked to make soon"
        );
    }

    private boolean slotFit(MealUsageAggregate aggregate, PlanningContext context) {
        if (context == null || context.mealType() == null) {
            return false;
        }
        return aggregate.usedMealTypes().contains(context.mealType());
    }

    private static final class AggregateAccumulator {
        private final MealIdentity identity;
        private final List<MealOccurrence> occurrences = new ArrayList<>();
        private final Set<String> distinctWeeks = new HashSet<>();
        private final Set<MealType> usedMealTypes = EnumSet.noneOf(MealType.class);
        private LocalDate lastPlannedDate = null;

        private AggregateAccumulator(MealIdentity identity) {
            this.identity = identity;
        }

        private MealIdentity identity() {
            return identity;
        }

        private void add(MealOccurrence occurrence) {
            occurrences.add(occurrence);
            if (lastPlannedDate == null || occurrence.plannedDate().isAfter(lastPlannedDate)) {
                lastPlannedDate = occurrence.plannedDate();
            }
            distinctWeeks.add(occurrence.year() + "-" + occurrence.isoWeek());
            usedMealTypes.add(occurrence.mealType());
        }

        private MealUsageAggregate toAggregate(
                LocalDate referenceDate,
                Recipe recipe,
                PreferenceResolution preferenceResolution
        ) {
            int recentOccurrences = (int) occurrences.stream()
                    .filter(occurrence -> !occurrence.plannedDate().isBefore(referenceDate.minusDays(RECENT_USAGE_WINDOW_DAYS)))
                    .count();
            boolean recent = !lastPlannedDate.isBefore(referenceDate.minusDays(RECENT_WINDOW_DAYS));
            boolean frequent = occurrences.size() >= 3;
            boolean familiar = occurrences.size() >= 2 && distinctWeeks.size() >= 2;
            boolean fallback = preferenceResolution.fallback() || (occurrences.size() >= 4 && distinctWeeks.size() >= 2);
            boolean preferenceFit = preferenceResolution.preferred() || preferenceResolution.fallback();
            boolean deprioritized = preferenceResolution.deprioritized();
            boolean makeSoon = recipe != null && recipe.getMakeSoonAt() != null;
            return new MealUsageAggregate(
                    identity,
                    occurrences.size(),
                    recentOccurrences,
                    distinctWeeks.size(),
                    lastPlannedDate,
                    usedMealTypes,
                    recent,
                    frequent,
                    familiar,
                    fallback,
                    preferenceFit,
                    deprioritized,
                    makeSoon
            );
        }
    }

    private static final class RecipeAccumulator {
        private final UUID recipeId;
        private final String recipeTitle;
        private final List<MealOccurrence> occurrences = new ArrayList<>();
        private final Set<String> distinctWeeks = new HashSet<>();
        private LocalDate lastUsedDate = null;

        private RecipeAccumulator(UUID recipeId, String recipeTitle) {
            this.recipeId = recipeId;
            this.recipeTitle = recipeTitle;
        }

        private UUID recipeId() {
            return recipeId;
        }

        private String recipeTitle() {
            return recipeTitle;
        }

        private void add(MealOccurrence occurrence) {
            occurrences.add(occurrence);
            if (lastUsedDate == null || occurrence.plannedDate().isAfter(lastUsedDate)) {
                lastUsedDate = occurrence.plannedDate();
            }
            distinctWeeks.add(occurrence.year() + "-" + occurrence.isoWeek());
        }

        private RecipeUsageHistory toHistory(
                LocalDate referenceDate,
                Recipe recipe,
                PreferenceResolution preferenceResolution
        ) {
            int recentUses = (int) occurrences.stream()
                    .filter(occurrence -> !occurrence.plannedDate().isBefore(referenceDate.minusDays(RECENT_USAGE_WINDOW_DAYS)))
                    .count();
            boolean frequent = occurrences.size() >= 3;
            boolean familiar = occurrences.size() >= 2 && distinctWeeks.size() >= 2;
            return new RecipeUsageHistory(
                    recipeId,
                    recipeTitle,
                    lastUsedDate,
                    occurrences.size(),
                    recentUses,
                    distinctWeeks.size(),
                    frequent,
                    familiar,
                    recipe != null && recipe.getMakeSoonAt() != null,
                    preferenceResolution.preferred() || preferenceResolution.fallback(),
                    preferenceResolution.deprioritized()
            );
        }
    }

    private record PreferenceResolution(boolean preferred, boolean fallback, boolean deprioritized) {
    }

    private static final class PreferenceIndex {
        private final Map<UUID, Set<HouseholdPreferenceSignalType>> recipeSignals;
        private final Map<String, Set<HouseholdPreferenceSignalType>> mealIdentitySignals;

        private PreferenceIndex(
                Map<UUID, Set<HouseholdPreferenceSignalType>> recipeSignals,
                Map<String, Set<HouseholdPreferenceSignalType>> mealIdentitySignals
        ) {
            this.recipeSignals = recipeSignals;
            this.mealIdentitySignals = mealIdentitySignals;
        }

        private static PreferenceIndex from(List<HouseholdPreferenceSignal> preferenceSignals) {
            Map<UUID, Set<HouseholdPreferenceSignalType>> recipeSignals = new HashMap<>();
            Map<String, Set<HouseholdPreferenceSignalType>> mealIdentitySignals = new HashMap<>();
            for (HouseholdPreferenceSignal signal : preferenceSignals) {
                if (signal.getTargetKind() == HouseholdPreferenceSignalTargetKind.RECIPE) {
                    recipeSignals.computeIfAbsent(signal.getRecipeId(), ignored -> EnumSet.noneOf(HouseholdPreferenceSignalType.class))
                            .add(signal.getSignalType());
                } else {
                    mealIdentitySignals.computeIfAbsent(signal.getMealIdentityKey(), ignored -> EnumSet.noneOf(HouseholdPreferenceSignalType.class))
                            .add(signal.getSignalType());
                }
            }
            return new PreferenceIndex(recipeSignals, mealIdentitySignals);
        }

        private PreferenceResolution resolve(MealIdentity identity) {
            Set<HouseholdPreferenceSignalType> resolved = EnumSet.noneOf(HouseholdPreferenceSignalType.class);
            if (identity.recipeId() != null) {
                resolved.addAll(recipeSignals.getOrDefault(identity.recipeId(), Set.of()));
            }
            resolved.addAll(mealIdentitySignals.getOrDefault(identity.key(), Set.of()));
            return new PreferenceResolution(
                    resolved.contains(HouseholdPreferenceSignalType.PREFER),
                    resolved.contains(HouseholdPreferenceSignalType.FALLBACK),
                    resolved.contains(HouseholdPreferenceSignalType.DEPRIORITIZE)
            );
        }
    }
}
