package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.domain.HouseholdPreferenceSignalTargetKind;
import app.lifelinq.features.meals.domain.HouseholdPreferenceSignalType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "meal_preference_signals",
        indexes = {
                @Index(name = "idx_meal_preference_signals_group_id", columnList = "group_id")
        }
)
public class HouseholdPreferenceSignalEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_kind", nullable = false, length = 32)
    private HouseholdPreferenceSignalTargetKind targetKind;

    @Column(name = "recipe_id")
    private UUID recipeId;

    @Column(name = "meal_identity_key", length = 255)
    private String mealIdentityKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "signal_type", nullable = false, length = 32)
    private HouseholdPreferenceSignalType signalType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected HouseholdPreferenceSignalEntity() {
    }

    HouseholdPreferenceSignalEntity(
            UUID id,
            UUID groupId,
            HouseholdPreferenceSignalTargetKind targetKind,
            UUID recipeId,
            String mealIdentityKey,
            HouseholdPreferenceSignalType signalType,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.groupId = groupId;
        this.targetKind = targetKind;
        this.recipeId = recipeId;
        this.mealIdentityKey = mealIdentityKey;
        this.signalType = signalType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    UUID getId() {
        return id;
    }

    UUID getGroupId() {
        return groupId;
    }

    HouseholdPreferenceSignalTargetKind getTargetKind() {
        return targetKind;
    }

    UUID getRecipeId() {
        return recipeId;
    }

    String getMealIdentityKey() {
        return mealIdentityKey;
    }

    HouseholdPreferenceSignalType getSignalType() {
        return signalType;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    Instant getUpdatedAt() {
        return updatedAt;
    }
}
