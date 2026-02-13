package app.lifelinq.features.meals.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(
        name = "planned_meals",
        indexes = {
                @jakarta.persistence.Index(name = "idx_planned_meals_week_plan_id", columnList = "week_plan_id"),
                @jakarta.persistence.Index(name = "idx_planned_meals_week_plan_day", columnList = "week_plan_id,day_of_week,meal_type")
        }
)
public class PlannedMealEntity {
    @EmbeddedId
    private PlannedMealId id;

    @ManyToOne(optional = false)
    @MapsId("weekPlanId")
    @JoinColumn(name = "week_plan_id", nullable = false)
    private WeekPlanEntity weekPlan;


    @Column(name = "recipe_id", nullable = false)
    private UUID recipeId;

    @Column(name = "recipe_title", nullable = false)
    private String recipeTitle;

    protected PlannedMealEntity() {
    }

    PlannedMealEntity(
            PlannedMealId id,
            WeekPlanEntity weekPlan,
            UUID recipeId,
            String recipeTitle
    ) {
        this.id = id;
        this.weekPlan = weekPlan;
        this.recipeId = recipeId;
        this.recipeTitle = recipeTitle;
    }

    PlannedMealId getId() {
        return id;
    }

    WeekPlanEntity getWeekPlan() {
        return weekPlan;
    }

    int getDayOfWeek() {
        return id.getDayOfWeek();
    }

    String getMealType() {
        return id.getMealType();
    }

    UUID getRecipeId() {
        return recipeId;
    }

    String getRecipeTitle() {
        return recipeTitle;
    }
}
