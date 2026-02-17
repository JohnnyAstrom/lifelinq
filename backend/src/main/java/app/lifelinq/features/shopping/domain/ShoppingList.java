package app.lifelinq.features.shopping.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ShoppingList {
    private final UUID id;
    private final UUID householdId;
    private String name;
    private int orderIndex;
    private final Instant createdAt;
    private final List<ShoppingItem> items;

    public ShoppingList(UUID id, UUID householdId, String name, Instant createdAt) {
        this(id, householdId, name, 0, createdAt, List.of());
    }

    public ShoppingList(UUID id, UUID householdId, String name, int orderIndex, Instant createdAt) {
        this(id, householdId, name, orderIndex, createdAt, List.of());
    }

    public ShoppingList(UUID id, UUID householdId, String name, Instant createdAt, List<ShoppingItem> items) {
        this(id, householdId, name, 0, createdAt, items);
    }

    public ShoppingList(
            UUID id,
            UUID householdId,
            String name,
            int orderIndex,
            Instant createdAt,
            List<ShoppingItem> items
    ) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
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
        if (items == null) {
            throw new IllegalArgumentException("items must not be null");
        }
        this.id = id;
        this.householdId = householdId;
        this.name = name;
        this.orderIndex = orderIndex;
        this.createdAt = createdAt;
        this.items = new ArrayList<>(items);
    }

    public UUID addItem(UUID itemId, String normalizedName, Instant now) {
        return addItem(itemId, normalizedName, null, null, now);
    }

    public UUID addItem(
            UUID itemId,
            String normalizedName,
            BigDecimal quantity,
            ShoppingUnit unit,
            Instant now
    ) {
        if (itemId == null) {
            throw new IllegalArgumentException("itemId must not be null");
        }
        if (normalizedName == null || normalizedName.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (now == null) {
            throw new IllegalArgumentException("now must not be null");
        }
        ShoppingItem item = new ShoppingItem(itemId, normalizedName, now, quantity, unit);
        items.add(item);
        return itemId;
    }

    public void toggleItem(UUID itemId, Instant now) {
        ShoppingItem item = findItemOrThrow(itemId);
        item.toggle(now);
    }

    public void updateItem(UUID itemId, String normalizedName, BigDecimal quantity, ShoppingUnit unit) {
        if (normalizedName == null || normalizedName.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        ShoppingItem item = findItemOrThrow(itemId);
        item.updateDetails(normalizedName, quantity, unit);
    }

    public void removeItem(UUID itemId) {
        if (itemId == null) {
            throw new IllegalArgumentException("itemId must not be null");
        }
        boolean removed = items.removeIf(item -> item.getId().equals(itemId));
        if (!removed) {
            throw new ShoppingItemNotFoundException(itemId);
        }
    }

    public void rename(String normalizedName) {
        if (normalizedName == null || normalizedName.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        this.name = normalizedName;
    }

    public void setOrderIndex(int orderIndex) {
        if (orderIndex < 0) {
            throw new IllegalArgumentException("orderIndex must be >= 0");
        }
        this.orderIndex = orderIndex;
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

    public int getOrderIndex() {
        return orderIndex;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<ShoppingItem> getItems() {
        return List.copyOf(items);
    }

    public ShoppingItem getItemOrThrow(UUID itemId) {
        return findItemOrThrow(itemId);
    }

    private ShoppingItem findItemOrThrow(UUID itemId) {
        if (itemId == null) {
            throw new IllegalArgumentException("itemId must not be null");
        }
        return items.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ShoppingItemNotFoundException(itemId));
    }

}
