package app.lifelinq.features.economy.infrastructure;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "settlement_period_participants")
public class SettlementPeriodParticipantEntity {
    @EmbeddedId
    private SettlementPeriodParticipantEntityId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("periodId")
    @JoinColumn(name = "period_id", nullable = false)
    private SettlementPeriodEntity period;

    protected SettlementPeriodParticipantEntity() {
    }

    public SettlementPeriodParticipantEntity(SettlementPeriodParticipantEntityId id, SettlementPeriodEntity period) {
        this.id = id;
        this.period = period;
    }

    public SettlementPeriodParticipantEntityId getId() {
        return id;
    }

    public SettlementPeriodEntity getPeriod() {
        return period;
    }
}
