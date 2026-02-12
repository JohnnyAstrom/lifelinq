package app.lifelinq.features.meals.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class WeekPlan {
    private final UUID id;
    private final UUID householdId;
    private final int year;
    private final int isoWeek;
    private final Instant createdAt;
    private final Map<Integer, PlannedMeal> mealsByDay;

    public WeekPlan(
            UUID id,
            UUID householdId,
            int year,
            int isoWeek,
            Instant createdAt
    ) {
        this(id, householdId, year, isoWeek, createdAt, Map.of());
    }

    public WeekPlan(
            UUID id,
            UUID householdId,
            int year,
            int isoWeek,
            Instant createdAt,
            Map<Integer, PlannedMeal> mealsByDay
    ) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        if (isoWeek < 1 || isoWeek > 53) {
            throw new IllegalArgumentException("isoWeek must be between 1 and 53");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }
        if (mealsByDay == null) {
            throw new IllegalArgumentException("mealsByDay must not be null");
        }
        this.id = id;
        this.householdId = householdId;
        this.year = year;
        this.isoWeek = isoWeek;
        this.createdAt = createdAt;
        this.mealsByDay = new HashMap<>();
        for (Map.Entry<Integer, PlannedMeal> entry : mealsByDay.entrySet()) {
            Integer key = entry.getKey();
            PlannedMeal meal = entry.getValue();
            if (key == null || meal == null) {
                throw new IllegalArgumentException("mealsByDay must not contain null keys or values");
            }
            if (key < 1 || key > 7) {
                throw new IllegalArgumentException("dayOfWeek must be between 1 and 7");
            }
            if (key != meal.getDayOfWeek()) {
                throw new IllegalArgumentException("mealsByDay key must match meal dayOfWeek");
            }
            this.mealsByDay.put(key, meal);
        }
    }

    public void addOrReplaceMeal(int dayOfWeek, RecipeRef recipeRef) {
        PlannedMeal meal = new PlannedMeal(dayOfWeek, recipeRef);
        // Replace is intentional: one meal per day.
        mealsByDay.put(dayOfWeek, meal);
    }

    public void removeMeal(int dayOfWeek) {
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new IllegalArgumentException("dayOfWeek must be between 1 and 7");
        }
        PlannedMeal removed = mealsByDay.remove(dayOfWeek);
        if (removed == null) {
            throw new IllegalArgumentException("no meal planned for dayOfWeek: " + dayOfWeek);
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getHouseholdId() {
        return householdId;
    }

    public int getYear() {
        return year;
    }

    public int getIsoWeek() {
        return isoWeek;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<PlannedMeal> getMeals() {
        List<PlannedMeal> meals = new ArrayList<>(mealsByDay.values());
        meals.sort((a, b) -> Integer.compare(a.getDayOfWeek(), b.getDayOfWeek()));
        return List.copyOf(meals);
    }

    public PlannedMeal getMealOrThrow(int dayOfWeek) {
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new IllegalArgumentException("dayOfWeek must be between 1 and 7");
        }
        PlannedMeal meal = mealsByDay.get(dayOfWeek);
        if (meal == null) {
            throw new IllegalArgumentException("no meal planned for dayOfWeek: " + dayOfWeek);
        }
        return meal;
    }
}
