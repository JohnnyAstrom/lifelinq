package app.lifelinq.features.shopping.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ShoppingList {
    private final UUID id;
    private final UUID householdId;
    private final String name;
    private final Instant createdAt;
    private final List<ShoppingItem> items;

    public ShoppingList(UUID id, UUID householdId, String name, Instant createdAt) {
        this(id, householdId, name, createdAt, List.of());
    }

    public ShoppingList(UUID id, UUID householdId, String name, Instant createdAt, List<ShoppingItem> items) {
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
        if (items == null) {
            throw new IllegalArgumentException("items must not be null");
        }
        this.id = id;
        this.householdId = householdId;
        this.name = name;
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
        ensureUniqueName(normalizedName);
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
        if (!item.getName().equals(normalizedName)) {
            ensureUniqueName(normalizedName);
        }
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

    private void ensureUniqueName(String normalizedName) {
        boolean exists = items.stream()
                .anyMatch(item -> item.getName().equals(normalizedName));
        if (exists) {
            throw new DuplicateShoppingItemNameException(normalizedName);
        }
    }
}
