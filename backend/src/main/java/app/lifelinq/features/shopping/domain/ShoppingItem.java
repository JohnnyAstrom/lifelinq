package app.lifelinq.features.shopping.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class ShoppingItem {
    private final UUID id;
    private String name;
    private final Instant createdAt;
    private ShoppingItemStatus status;
    private Instant boughtAt;
    private BigDecimal quantity;
    private ShoppingUnit unit;

    public ShoppingItem(UUID id, String name, Instant createdAt) {
        this(id, name, createdAt, null, null);
    }

    public ShoppingItem(UUID id, String name, Instant createdAt, BigDecimal quantity, ShoppingUnit unit) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }
        validateQuantityAndUnit(quantity, unit);
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.status = ShoppingItemStatus.TO_BUY;
        this.boughtAt = null;
        this.quantity = quantity;
        this.unit = unit;
    }

    public static ShoppingItem rehydrate(
            UUID id,
            String name,
            Instant createdAt,
            ShoppingItemStatus status,
            Instant boughtAt,
            BigDecimal quantity,
            ShoppingUnit unit
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
        ShoppingItem item = new ShoppingItem(id, name, createdAt, quantity, unit);
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

    public void updateDetails(String name, BigDecimal quantity, ShoppingUnit unit) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        validateQuantityAndUnit(quantity, unit);
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
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

    public BigDecimal getQuantity() {
        return quantity;
    }

    public ShoppingUnit getUnit() {
        return unit;
    }

    private static void validateQuantityAndUnit(BigDecimal quantity, ShoppingUnit unit) {
        if (quantity == null && unit == null) {
            return;
        }
        if (quantity == null || unit == null) {
            throw new IllegalArgumentException("quantity and unit must be provided together");
        }
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("quantity must be greater than 0");
        }
    }
}
