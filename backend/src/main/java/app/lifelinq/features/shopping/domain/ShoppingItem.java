package app.lifelinq.features.shopping.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class ShoppingItem {
    private final UUID id;
    private String name;
    private int orderIndex;
    private final Instant createdAt;
    private ShoppingItemStatus status;
    private Instant boughtAt;
    private BigDecimal quantity;
    private ShoppingUnit unit;
    private ShoppingItemSourceKind sourceKind;
    private String sourceLabel;

    public ShoppingItem(UUID id, String name, Instant createdAt) {
        this(id, name, 0, createdAt, null, null, null, null);
    }

    public ShoppingItem(UUID id, String name, Instant createdAt, BigDecimal quantity, ShoppingUnit unit) {
        this(id, name, 0, createdAt, quantity, unit, null, null);
    }

    public ShoppingItem(
            UUID id,
            String name,
            Instant createdAt,
            BigDecimal quantity,
            ShoppingUnit unit,
            ShoppingItemSourceKind sourceKind,
            String sourceLabel
    ) {
        this(id, name, 0, createdAt, quantity, unit, sourceKind, sourceLabel);
    }

    public ShoppingItem(
            UUID id,
            String name,
            int orderIndex,
            Instant createdAt,
            BigDecimal quantity,
            ShoppingUnit unit,
            ShoppingItemSourceKind sourceKind,
            String sourceLabel
    ) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (orderIndex < 0) {
            throw new IllegalArgumentException("orderIndex must be >= 0");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }
        validateQuantityAndUnit(quantity, unit);
        validateSource(sourceKind, sourceLabel);
        this.id = id;
        this.name = name;
        this.orderIndex = orderIndex;
        this.createdAt = createdAt;
        this.status = ShoppingItemStatus.TO_BUY;
        this.boughtAt = null;
        this.quantity = quantity;
        this.unit = unit;
        this.sourceKind = sourceKind;
        this.sourceLabel = normalizeSourceLabel(sourceLabel);
    }

    public static ShoppingItem rehydrate(
            UUID id,
            String name,
            int orderIndex,
            Instant createdAt,
            ShoppingItemStatus status,
            Instant boughtAt,
            BigDecimal quantity,
            ShoppingUnit unit,
            ShoppingItemSourceKind sourceKind,
            String sourceLabel
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
        ShoppingItem item = new ShoppingItem(id, name, orderIndex, createdAt, quantity, unit, sourceKind, sourceLabel);
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

    public boolean canAbsorbMealPlanIntake(BigDecimal incomingQuantity, ShoppingUnit incomingUnit) {
        if (status != ShoppingItemStatus.TO_BUY) {
            return false;
        }
        if (quantity == null && unit == null && incomingQuantity == null && incomingUnit == null) {
            return true;
        }
        if (quantity == null || unit == null) {
            return false;
        }
        if (incomingQuantity == null || incomingUnit == null) {
            return false;
        }
        return unit == incomingUnit;
    }

    public void absorbMealPlanIntake(BigDecimal incomingQuantity, ShoppingUnit incomingUnit) {
        if (!canAbsorbMealPlanIntake(incomingQuantity, incomingUnit)) {
            throw new IllegalArgumentException("incoming meal-plan item is not compatible with existing item");
        }
        if (quantity != null && incomingQuantity != null) {
            quantity = quantity.add(incomingQuantity);
        }
        clearSource();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        if (orderIndex < 0) {
            throw new IllegalArgumentException("orderIndex must be >= 0");
        }
        this.orderIndex = orderIndex;
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

    public ShoppingItemSourceKind getSourceKind() {
        return sourceKind;
    }

    public String getSourceLabel() {
        return sourceLabel;
    }

    private void clearSource() {
        sourceKind = null;
        sourceLabel = null;
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

    private static void validateSource(ShoppingItemSourceKind sourceKind, String sourceLabel) {
        String normalizedLabel = normalizeSourceLabel(sourceLabel);
        if (sourceKind == null && normalizedLabel != null) {
            throw new IllegalArgumentException("sourceLabel requires sourceKind");
        }
    }

    private static String normalizeSourceLabel(String sourceLabel) {
        if (sourceLabel == null) {
            return null;
        }
        String normalized = sourceLabel.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
