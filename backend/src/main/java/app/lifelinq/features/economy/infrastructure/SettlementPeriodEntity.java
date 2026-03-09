package app.lifelinq.features.economy.infrastructure;

import app.lifelinq.features.economy.domain.SettlementPeriodStatus;
import app.lifelinq.features.economy.domain.SettlementStrategyType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "settlement_periods")
public class SettlementPeriodEntity {
    @Id
    private UUID id;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementPeriodStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "strategy_type", nullable = false)
    private SettlementStrategyType strategyType;

    @Column(name = "strategy_snapshot_json", nullable = false)
    private String strategySnapshotJson;

    @OneToMany(mappedBy = "period", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SettlementPeriodParticipantEntity> participants = new LinkedHashSet<>();

    protected SettlementPeriodEntity() {
    }

    public SettlementPeriodEntity(
            UUID id,
            UUID groupId,
            Instant startDate,
            Instant endDate,
            SettlementPeriodStatus status,
            SettlementStrategyType strategyType,
            String strategySnapshotJson
    ) {
        this.id = id;
        this.groupId = groupId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.strategyType = strategyType;
        this.strategySnapshotJson = strategySnapshotJson;
    }

    public UUID getId() {
        return id;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public SettlementPeriodStatus getStatus() {
        return status;
    }

    public SettlementStrategyType getStrategyType() {
        return strategyType;
    }

    public String getStrategySnapshotJson() {
        return strategySnapshotJson;
    }

    public Set<SettlementPeriodParticipantEntity> getParticipants() {
        return participants;
    }

    public void replaceParticipants(Set<SettlementPeriodParticipantEntity> participants) {
        this.participants.clear();
        this.participants.addAll(participants);
    }
}
