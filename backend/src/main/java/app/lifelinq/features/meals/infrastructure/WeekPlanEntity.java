package app.lifelinq.features.meals.infrastructure;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "week_plans",
        uniqueConstraints = {
                @jakarta.persistence.UniqueConstraint(columnNames = {"group_id", "week_year", "iso_week"})
        },
        indexes = {
                @jakarta.persistence.Index(name = "idx_week_plans_group_id", columnList = "group_id")
        }
)
public class WeekPlanEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "week_year", nullable = false)
    private int year;

    @Column(name = "iso_week", nullable = false)
    private int isoWeek;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "shopping_review_handled_at")
    private Instant shoppingReviewHandledAt;

    @Column(name = "shopping_review_list_id")
    private UUID shoppingReviewListId;

    @OneToMany(
            mappedBy = "weekPlan",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PlannedMealEntity> meals = new ArrayList<>();

    protected WeekPlanEntity() {
    }

    WeekPlanEntity(
            UUID id,
            UUID groupId,
            int year,
            int isoWeek,
            Instant createdAt,
            Instant shoppingReviewHandledAt,
            UUID shoppingReviewListId
    ) {
        this.id = id;
        this.groupId = groupId;
        this.year = year;
        this.isoWeek = isoWeek;
        this.createdAt = createdAt;
        this.shoppingReviewHandledAt = shoppingReviewHandledAt;
        this.shoppingReviewListId = shoppingReviewListId;
    }

    UUID getId() {
        return id;
    }

    UUID getGroupId() {
        return groupId;
    }

    int getYear() {
        return year;
    }

    int getIsoWeek() {
        return isoWeek;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    Instant getShoppingReviewHandledAt() {
        return shoppingReviewHandledAt;
    }

    void setShoppingReviewHandledAt(Instant shoppingReviewHandledAt) {
        this.shoppingReviewHandledAt = shoppingReviewHandledAt;
    }

    UUID getShoppingReviewListId() {
        return shoppingReviewListId;
    }

    void setShoppingReviewListId(UUID shoppingReviewListId) {
        this.shoppingReviewListId = shoppingReviewListId;
    }

    List<PlannedMealEntity> getMeals() {
        return meals;
    }
}
