package app.lifelinq.features.shopping.domain;

import java.time.Instant;
import java.util.UUID;

public final class ShoppingItem {
    private final UUID id;
    private final String name;
    private final Instant createdAt;
    private ShoppingItemStatus status;
    private Instant boughtAt;

    public ShoppingItem(UUID id, String name, Instant createdAt) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.status = ShoppingItemStatus.TO_BUY;
        this.boughtAt = null;
    }

    public static ShoppingItem rehydrate(
            UUID id,
            String name,
            Instant createdAt,
            ShoppingItemStatus status,
            Instant boughtAt
    ) {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        if (status == ShoppingItemStatus.TO_BUY && boughtAt != null) {
            throw new IllegalArgumentException("boughtAt must be null when status is TO_BUY");
        }
        if (status == ShoppingItemStatus.BOUGHT && boughtAt == null) {
            throw new IllegalArgumentException("boughtAt must not be null when status is BOUGHT");
        }
        ShoppingItem item = new ShoppingItem(id, name, createdAt);
        item.status = status;
        item.boughtAt = boughtAt;
        return item;
    }

    public void toggle(Instant now) {
        if (now == null) {
            throw new IllegalArgumentException("now must not be null");
        }
        if (status == ShoppingItemStatus.TO_BUY) {
            status = ShoppingItemStatus.BOUGHT;
            boughtAt = now;
        } else {
            status = ShoppingItemStatus.TO_BUY;
            boughtAt = null;
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ShoppingItemStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getBoughtAt() {
        return boughtAt;
    }
}
