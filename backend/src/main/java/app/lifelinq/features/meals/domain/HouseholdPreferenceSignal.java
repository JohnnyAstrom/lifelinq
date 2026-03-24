package app.lifelinq.features.meals.domain;

import java.time.Instant;
import java.util.UUID;

public final class HouseholdPreferenceSignal {
    private final UUID id;
    private final UUID groupId;
    private final HouseholdPreferenceSignalTargetKind targetKind;
    private final UUID recipeId;
    private final String mealIdentityKey;
    private final HouseholdPreferenceSignalType signalType;
    private final Instant createdAt;
    private final Instant updatedAt;

    public HouseholdPreferenceSignal(
            UUID id,
            UUID groupId,
            HouseholdPreferenceSignalTargetKind targetKind,
            UUID recipeId,
            String mealIdentityKey,
            HouseholdPreferenceSignalType signalType,
            Instant createdAt,
            Instant updatedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        if (targetKind == null) {
            throw new IllegalArgumentException("targetKind must not be null");
        }
        if (signalType == null) {
            throw new IllegalArgumentException("signalType must not be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }
        if (updatedAt == null) {
            throw new IllegalArgumentException("updatedAt must not be null");
        }
        if (updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("updatedAt must not be before createdAt");
        }
        switch (targetKind) {
            case RECIPE -> {
                if (recipeId == null) {
                    throw new IllegalArgumentException("recipeId must not be null for recipe preference signals");
                }
                if (mealIdentityKey != null && !mealIdentityKey.isBlank()) {
                    throw new IllegalArgumentException("mealIdentityKey must be empty for recipe preference signals");
                }
            }
            case MEAL_IDENTITY -> {
                if (mealIdentityKey == null || mealIdentityKey.isBlank()) {
                    throw new IllegalArgumentException("mealIdentityKey must not be blank for meal identity preference signals");
                }
                if (recipeId != null) {
                    throw new IllegalArgumentException("recipeId must be null for meal identity preference signals");
                }
            }
        }
        this.id = id;
        this.groupId = groupId;
        this.targetKind = targetKind;
        this.recipeId = recipeId;
        this.mealIdentityKey = normalizeOptional(mealIdentityKey);
        this.signalType = signalType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public UUID getId() {
        return id;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public HouseholdPreferenceSignalTargetKind getTargetKind() {
        return targetKind;
    }

    public UUID getRecipeId() {
        return recipeId;
    }

    public String getMealIdentityKey() {
        return mealIdentityKey;
    }

    public HouseholdPreferenceSignalType getSignalType() {
        return signalType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
