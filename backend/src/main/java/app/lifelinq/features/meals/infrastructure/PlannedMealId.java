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

    protected PlannedMealId() {
    }

    PlannedMealId(UUID weekPlanId, int dayOfWeek) {
        this.weekPlanId = weekPlanId;
        this.dayOfWeek = dayOfWeek;
    }

    UUID getWeekPlanId() {
        return weekPlanId;
    }

    int getDayOfWeek() {
        return dayOfWeek;
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
        return dayOfWeek == that.dayOfWeek && Objects.equals(weekPlanId, that.weekPlanId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(weekPlanId, dayOfWeek);
    }
}
