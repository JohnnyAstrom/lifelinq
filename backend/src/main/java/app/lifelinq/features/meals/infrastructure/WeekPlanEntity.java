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
                @jakarta.persistence.UniqueConstraint(columnNames = {"household_id", "week_year", "iso_week"})
        },
        indexes = {
                @jakarta.persistence.Index(name = "idx_week_plans_household_id", columnList = "household_id")
        }
)
public class WeekPlanEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "household_id", nullable = false)
    private UUID householdId;

    @Column(name = "week_year", nullable = false)
    private int year;

    @Column(name = "iso_week", nullable = false)
    private int isoWeek;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(
            mappedBy = "weekPlan",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PlannedMealEntity> meals = new ArrayList<>();

    protected WeekPlanEntity() {
    }

    WeekPlanEntity(UUID id, UUID householdId, int year, int isoWeek, Instant createdAt) {
        this.id = id;
        this.householdId = householdId;
        this.year = year;
        this.isoWeek = isoWeek;
        this.createdAt = createdAt;
    }

    UUID getId() {
        return id;
    }

    UUID getHouseholdId() {
        return householdId;
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

    List<PlannedMealEntity> getMeals() {
        return meals;
    }
}
