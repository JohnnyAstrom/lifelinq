package app.lifelinq.features.meals.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.Instant;
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

    @Column(name = "meal_title", nullable = false)
    private String mealTitle;

    @Column(name = "recipe_id")
    private UUID recipeId;

    @Column(name = "recipe_title_snapshot")
    private String recipeTitleSnapshot;

    @Column(name = "shopping_handled_at")
    private Instant shoppingHandledAt;

    @Column(name = "shopping_list_id")
    private UUID shoppingListId;

    protected PlannedMealEntity() {
    }

    PlannedMealEntity(
            PlannedMealId id,
            WeekPlanEntity weekPlan,
            String mealTitle,
            UUID recipeId,
            String recipeTitleSnapshot,
            Instant shoppingHandledAt,
            UUID shoppingListId
    ) {
        this.id = id;
        this.weekPlan = weekPlan;
        this.mealTitle = mealTitle;
        this.recipeId = recipeId;
        this.recipeTitleSnapshot = recipeTitleSnapshot;
        this.shoppingHandledAt = shoppingHandledAt;
        this.shoppingListId = shoppingListId;
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

    String getMealTitle() {
        return mealTitle;
    }

    UUID getRecipeId() {
        return recipeId;
    }

    String getRecipeTitleSnapshot() {
        return recipeTitleSnapshot;
    }

    Instant getShoppingHandledAt() {
        return shoppingHandledAt;
    }

    UUID getShoppingListId() {
        return shoppingListId;
    }
}
