package app.lifelinq.features.shopping.domain;

import java.time.Instant;
import java.util.UUID;

public final class ShoppingItem {
    private final UUID id;
    private final UUID householdId;
    private final String name;
    private final Instant createdAt;

    public ShoppingItem(UUID id, UUID householdId, String name, Instant createdAt) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }
        this.id = id;
        this.householdId = householdId;
        this.name = name;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getHouseholdId() {
        return householdId;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
