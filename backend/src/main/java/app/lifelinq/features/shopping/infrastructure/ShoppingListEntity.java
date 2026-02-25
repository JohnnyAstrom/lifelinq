package app.lifelinq.features.shopping.infrastructure;

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
        name = "shopping_lists",
        indexes = {
                @jakarta.persistence.Index(name = "idx_shopping_lists_household_id", columnList = "household_id")
        }
)
public class ShoppingListEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "household_id", nullable = false)
    private UUID groupId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(
            mappedBy = "list",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ShoppingItemEntity> items = new ArrayList<>();

    protected ShoppingListEntity() {
    }

    ShoppingListEntity(UUID id, UUID groupId, String name, int orderIndex, Instant createdAt) {
        this.id = id;
        this.groupId = groupId;
        this.name = name;
        this.orderIndex = orderIndex;
        this.createdAt = createdAt;
    }

    UUID getId() {
        return id;
    }

    UUID getGroupId() {
        return groupId;
    }

    String getName() {
        return name;
    }

    int getOrderIndex() {
        return orderIndex;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    List<ShoppingItemEntity> getItems() {
        return items;
    }
}
