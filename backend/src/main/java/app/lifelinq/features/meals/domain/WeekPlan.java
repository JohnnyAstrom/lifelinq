package app.lifelinq.features.meals.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class WeekPlan {
    public record DaySlot(int dayOfWeek, MealType mealType) {
        public DaySlot {
            if (dayOfWeek < 1 || dayOfWeek > 7) {
                throw new IllegalArgumentException("dayOfWeek must be between 1 and 7");
            }
            if (mealType == null) {
                throw new IllegalArgumentException("mealType must not be null");
            }
        }
    }

    private final UUID id;
    private final UUID householdId;
    private final int year;
    private final int isoWeek;
    private final Instant createdAt;
    private final Map<DaySlot, PlannedMeal> mealsBySlot;

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
            Map<DaySlot, PlannedMeal> mealsByDay
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
        this.mealsBySlot = new HashMap<>();
        for (Map.Entry<DaySlot, PlannedMeal> entry : mealsByDay.entrySet()) {
            DaySlot key = entry.getKey();
            PlannedMeal meal = entry.getValue();
            if (key == null || meal == null) {
                throw new IllegalArgumentException("mealsByDay must not contain null keys or values");
            }
            if (key.dayOfWeek() != meal.getDayOfWeek()) {
                throw new IllegalArgumentException("mealsByDay key must match meal dayOfWeek");
            }
            if (key.mealType() != meal.getMealType()) {
                throw new IllegalArgumentException("mealsByDay key must match meal type");
            }
            this.mealsBySlot.put(key, meal);
        }
    }

    public void addOrReplaceMeal(int dayOfWeek, MealType mealType, RecipeRef recipeRef) {
        PlannedMeal meal = new PlannedMeal(dayOfWeek, mealType, recipeRef);
        mealsBySlot.put(new DaySlot(dayOfWeek, mealType), meal);
    }

    public void removeMeal(int dayOfWeek, MealType mealType) {
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new IllegalArgumentException("dayOfWeek must be between 1 and 7");
        }
        if (mealType == null) {
            throw new IllegalArgumentException("mealType must not be null");
        }
        PlannedMeal removed = mealsBySlot.remove(new DaySlot(dayOfWeek, mealType));
        if (removed == null) {
            throw new IllegalArgumentException("no meal planned for dayOfWeek: " + dayOfWeek + " and mealType: " + mealType);
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
        List<PlannedMeal> meals = new ArrayList<>(mealsBySlot.values());
        meals.sort((a, b) -> {
            int dayCompare = Integer.compare(a.getDayOfWeek(), b.getDayOfWeek());
            if (dayCompare != 0) {
                return dayCompare;
            }
            return Integer.compare(a.getMealType().ordinal(), b.getMealType().ordinal());
        });
        return List.copyOf(meals);
    }

    public PlannedMeal getMealOrThrow(int dayOfWeek, MealType mealType) {
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new IllegalArgumentException("dayOfWeek must be between 1 and 7");
        }
        if (mealType == null) {
            throw new IllegalArgumentException("mealType must not be null");
        }
        PlannedMeal meal = mealsBySlot.get(new DaySlot(dayOfWeek, mealType));
        if (meal == null) {
            throw new IllegalArgumentException("no meal planned for dayOfWeek: " + dayOfWeek + " and mealType: " + mealType);
        }
        return meal;
    }
}
