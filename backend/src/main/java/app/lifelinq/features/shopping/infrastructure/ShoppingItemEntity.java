package app.lifelinq.features.shopping.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "shopping_items",
        indexes = {
                @jakarta.persistence.Index(name = "idx_shopping_items_list_id", columnList = "list_id")
        }
)
public class ShoppingItemEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "list_id", nullable = false)
    private ShoppingListEntity list;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ShoppingItemStatusEntity status;

    @Column(name = "quantity")
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit")
    private ShoppingUnitEntity unit;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "bought_at")
    private Instant boughtAt;

    protected ShoppingItemEntity() {
    }

    ShoppingItemEntity(
            UUID id,
            ShoppingListEntity list,
            String name,
            ShoppingItemStatusEntity status,
            BigDecimal quantity,
            ShoppingUnitEntity unit,
            Instant createdAt,
            Instant boughtAt
    ) {
        this.id = id;
        this.list = list;
        this.name = name;
        this.status = status;
        this.quantity = quantity;
        this.unit = unit;
        this.createdAt = createdAt;
        this.boughtAt = boughtAt;
    }

    UUID getId() {
        return id;
    }

    ShoppingListEntity getList() {
        return list;
    }

    String getName() {
        return name;
    }

    ShoppingItemStatusEntity getStatus() {
        return status;
    }

    BigDecimal getQuantity() {
        return quantity;
    }

    ShoppingUnitEntity getUnit() {
        return unit;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    Instant getBoughtAt() {
        return boughtAt;
    }
}
