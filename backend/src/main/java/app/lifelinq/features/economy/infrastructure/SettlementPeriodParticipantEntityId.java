package app.lifelinq.features.economy.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class SettlementPeriodParticipantEntityId implements Serializable {
    @Column(name = "period_id", nullable = false)
    private UUID periodId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    protected SettlementPeriodParticipantEntityId() {
    }

    public SettlementPeriodParticipantEntityId(UUID periodId, UUID userId) {
        this.periodId = periodId;
        this.userId = userId;
    }

    public UUID getPeriodId() {
        return periodId;
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SettlementPeriodParticipantEntityId that)) {
            return false;
        }
        return Objects.equals(periodId, that.periodId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periodId, userId);
    }
}
