package app.lifelinq.features.meals.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class PlannedMealId implements Serializable {
    @Column(name = "week_plan_id", nullable = false)
    private UUID weekPlanId;

    @Column(name = "day_of_week", nullable = false)
    private int dayOfWeek;

    @Column(name = "meal_type", nullable = false)
    private String mealType;

    protected PlannedMealId() {
    }

    PlannedMealId(UUID weekPlanId, int dayOfWeek, String mealType) {
        this.weekPlanId = weekPlanId;
        this.dayOfWeek = dayOfWeek;
        this.mealType = mealType;
    }

    UUID getWeekPlanId() {
        return weekPlanId;
    }

    int getDayOfWeek() {
        return dayOfWeek;
    }

    String getMealType() {
        return mealType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlannedMealId that = (PlannedMealId) o;
        return dayOfWeek == that.dayOfWeek
                && Objects.equals(weekPlanId, that.weekPlanId)
                && Objects.equals(mealType, that.mealType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(weekPlanId, dayOfWeek, mealType);
    }
}
