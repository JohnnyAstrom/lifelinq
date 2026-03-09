package app.lifelinq.features.economy.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "settlement_transactions")
public class SettlementTransactionEntity {
    @Id
    private UUID id;

    @Column(name = "period_id", nullable = false)
    private UUID periodId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false)
    private String description;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    @Column(name = "paid_by_user_id", nullable = false)
    private UUID paidByUserId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column
    private String category;

    protected SettlementTransactionEntity() {
    }

    public SettlementTransactionEntity(
            UUID id,
            UUID periodId,
            BigDecimal amount,
            String description,
            UUID createdByUserId,
            UUID paidByUserId,
            Instant createdAt,
            Instant deletedAt,
            String category
    ) {
        this.id = id;
        this.periodId = periodId;
        this.amount = amount;
        this.description = description;
        this.createdByUserId = createdByUserId;
        this.paidByUserId = paidByUserId;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
        this.category = category;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPeriodId() {
        return periodId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public UUID getPaidByUserId() {
        return paidByUserId;
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public String getCategory() {
        return category;
    }
}
