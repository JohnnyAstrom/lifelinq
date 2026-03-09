package app.lifelinq.features.economy.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class SettlementPeriod {
    private final UUID id;
    private final UUID groupId;
    private final Instant startDate;
    private final Instant endDate;
    private final SettlementPeriodStatus status;
    private final SettlementStrategySnapshot strategySnapshot;
    private final Set<PeriodParticipant> participants;

    public SettlementPeriod(
            UUID id,
            UUID groupId,
            Instant startDate,
            Instant endDate,
            SettlementPeriodStatus status,
            SettlementStrategySnapshot strategySnapshot,
            Set<PeriodParticipant> participants
    ) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("startDate must not be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        if (strategySnapshot == null) {
            throw new IllegalArgumentException("strategySnapshot must not be null");
        }
        if (participants == null || participants.isEmpty()) {
            throw new IllegalArgumentException("participants must not be empty");
        }
        if (status == SettlementPeriodStatus.OPEN && endDate != null) {
            throw new IllegalArgumentException("OPEN period must not have endDate");
        }
        if (status == SettlementPeriodStatus.CLOSED && endDate == null) {
            throw new IllegalArgumentException("CLOSED period must have endDate");
        }
        Set<PeriodParticipant> uniqueParticipants = new LinkedHashSet<>(participants);
        if (uniqueParticipants.isEmpty()) {
            throw new IllegalArgumentException("participants must not be empty");
        }
        this.id = id;
        this.groupId = groupId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.strategySnapshot = strategySnapshot;
        this.participants = Collections.unmodifiableSet(uniqueParticipants);
    }

    public static SettlementPeriod createInitialOpenPeriod(
            UUID groupId,
            Instant startDate,
            Set<PeriodParticipant> participants
    ) {
        return createOpenPeriod(groupId, startDate, SettlementStrategySnapshot.equalCost(), participants);
    }

    public static SettlementPeriod createOpenPeriod(
            UUID groupId,
            Instant startDate,
            SettlementStrategySnapshot strategySnapshot,
            Set<PeriodParticipant> participants
    ) {
        return new SettlementPeriod(
                UUID.randomUUID(),
                groupId,
                startDate,
                null,
                SettlementPeriodStatus.OPEN,
                strategySnapshot,
                participants
        );
    }

    public SettlementPeriod close(Instant endDate) {
        if (endDate == null) {
            throw new IllegalArgumentException("endDate must not be null");
        }
        if (status != SettlementPeriodStatus.OPEN) {
            throw new IllegalStateException("only OPEN period can be closed");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must be >= startDate");
        }
        return new SettlementPeriod(
                id,
                groupId,
                startDate,
                endDate,
                SettlementPeriodStatus.CLOSED,
                strategySnapshot,
                participants
        );
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

    public SettlementStrategySnapshot getStrategySnapshot() {
        return strategySnapshot;
    }

    public List<PeriodParticipant> getParticipants() {
        return new ArrayList<>(participants);
    }

    public boolean isOpen() {
        return status == SettlementPeriodStatus.OPEN;
    }

    public boolean hasParticipant(UUID userId) {
        if (userId == null) {
            return false;
        }
        for (PeriodParticipant participant : participants) {
            if (userId.equals(participant.getUserId())) {
                return true;
            }
        }
        return false;
    }
}
