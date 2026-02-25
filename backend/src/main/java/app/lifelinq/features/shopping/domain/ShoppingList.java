package app.lifelinq.features.shopping.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class ShoppingList {
    private final UUID id;
    private final UUID groupId;
    private String name;
    private int orderIndex;
    private final Instant createdAt;
    private final List<ShoppingItem> items;

    public ShoppingList(UUID id, UUID groupId, String name, Instant createdAt) {
        this(id, groupId, name, 0, createdAt, List.of());
    }

    public ShoppingList(UUID id, UUID groupId, String name, int orderIndex, Instant createdAt) {
        this(id, groupId, name, orderIndex, createdAt, List.of());
    }

    public ShoppingList(UUID id, UUID groupId, String name, Instant createdAt, List<ShoppingItem> items) {
        this(id, groupId, name, 0, createdAt, items);
    }

    public ShoppingList(
            UUID id,
            UUID groupId,
            String name,
            int orderIndex,
            Instant createdAt,
            List<ShoppingItem> items
    ) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
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
        this.groupId = groupId;
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
        shiftItemOrderIndexesForInsertAtTop();
        ShoppingItem item = new ShoppingItem(itemId, normalizedName, 0, now, quantity, unit);
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
        normalizeItemOrderIndexes();
    }

    public void reorderOpenItem(UUID itemId, String direction) {
        if (itemId == null) {
            throw new IllegalArgumentException("itemId must not be null");
        }
        if (direction == null || direction.isBlank()) {
            throw new IllegalArgumentException("direction must not be blank");
        }
        String normalizedDirection = direction.trim().toUpperCase();
        if (!"UP".equals(normalizedDirection) && !"DOWN".equals(normalizedDirection)) {
            throw new IllegalArgumentException("direction must be UP or DOWN");
        }

        normalizeItemOrderIndexes();
        List<ShoppingItem> openItems = items.stream()
                .filter(item -> item.getStatus() == ShoppingItemStatus.TO_BUY)
                .sorted(Comparator.comparingInt(ShoppingItem::getOrderIndex))
                .toList();
        int currentOpenIndex = -1;
        for (int i = 0; i < openItems.size(); i++) {
            if (openItems.get(i).getId().equals(itemId)) {
                currentOpenIndex = i;
                break;
            }
        }
        if (currentOpenIndex < 0) {
            throw new ShoppingItemNotFoundException(itemId);
        }
        int targetOpenIndex = "UP".equals(normalizedDirection) ? currentOpenIndex - 1 : currentOpenIndex + 1;
        if (targetOpenIndex < 0 || targetOpenIndex >= openItems.size()) {
            return;
        }
        ShoppingItem current = openItems.get(currentOpenIndex);
        ShoppingItem target = openItems.get(targetOpenIndex);
        int currentOrder = current.getOrderIndex();
        current.setOrderIndex(target.getOrderIndex());
        target.setOrderIndex(currentOrder);
        normalizeItemOrderIndexes();
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

    public UUID getGroupId() {
        return groupId;
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
        List<ShoppingItem> sorted = new ArrayList<>(items);
        sorted.sort(Comparator
                .comparingInt(ShoppingItem::getOrderIndex)
                .thenComparing(ShoppingItem::getCreatedAt)
                .thenComparing(ShoppingItem::getId));
        return List.copyOf(sorted);
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

    private void shiftItemOrderIndexesForInsertAtTop() {
        for (ShoppingItem item : items) {
            item.setOrderIndex(item.getOrderIndex() + 1);
        }
    }

    private void normalizeItemOrderIndexes() {
        List<ShoppingItem> sorted = new ArrayList<>(items);
        sorted.sort(Comparator
                .comparingInt(ShoppingItem::getOrderIndex)
                .thenComparing(ShoppingItem::getCreatedAt)
                .thenComparing(ShoppingItem::getId));
        for (int index = 0; index < sorted.size(); index++) {
            sorted.get(index).setOrderIndex(index);
        }
    }

}
