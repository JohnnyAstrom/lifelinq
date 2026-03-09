package app.lifelinq.features.economy.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class SettlementTransaction {
    private final UUID id;
    private final UUID periodId;
    private final BigDecimal amount;
    private final String description;
    private final UUID createdByUserId;
    private final UUID paidByUserId;
    private final Instant createdAt;
    private final Instant deletedAt;
    private final String category;

    public SettlementTransaction(
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
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (periodId == null) {
            throw new IllegalArgumentException("periodId must not be null");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }
        if (paidByUserId == null) {
            throw new IllegalArgumentException("paidByUserId must not be null");
        }
        if (createdByUserId == null) {
            throw new IllegalArgumentException("createdByUserId must not be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }
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

    public static SettlementTransaction createForPeriod(
            SettlementPeriod period,
            BigDecimal amount,
            String description,
            UUID actorUserId,
            UUID paidByUserId,
            Instant createdAt,
            String category
    ) {
        if (period == null) {
            throw new IllegalArgumentException("period must not be null");
        }
        if (!period.isOpen()) {
            throw new IllegalStateException("period must be OPEN");
        }
        if (!period.hasParticipant(paidByUserId)) {
            throw new IllegalArgumentException("paidByUserId must be a participant of the period");
        }
        if (!period.hasParticipant(actorUserId)) {
            throw new IllegalArgumentException("createdByUserId must be a participant of the period");
        }
        return new SettlementTransaction(
                UUID.randomUUID(),
                period.getId(),
                amount,
                description,
                actorUserId,
                paidByUserId,
                createdAt,
                null,
                category
        );
    }

    public SettlementTransaction softDelete(Instant deletedAt, SettlementPeriod period) {
        if (deletedAt == null) {
            throw new IllegalArgumentException("deletedAt must not be null");
        }
        if (period == null) {
            throw new IllegalArgumentException("period must not be null");
        }
        if (!period.getId().equals(periodId)) {
            throw new IllegalArgumentException("period mismatch");
        }
        if (!period.isOpen()) {
            throw new IllegalStateException("soft delete is allowed only while period is OPEN");
        }
        if (this.deletedAt != null) {
            return this;
        }
        return new SettlementTransaction(
                id,
                periodId,
                amount,
                description,
                createdByUserId,
                paidByUserId,
                createdAt,
                deletedAt,
                category
        );
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
