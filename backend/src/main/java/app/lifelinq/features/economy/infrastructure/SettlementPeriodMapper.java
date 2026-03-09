package app.lifelinq.features.economy.infrastructure;

import app.lifelinq.features.economy.domain.PeriodParticipant;
import app.lifelinq.features.economy.domain.SettlementPeriod;
import app.lifelinq.features.economy.domain.SettlementStrategySnapshot;
import java.util.LinkedHashSet;
import java.util.Set;

public class SettlementPeriodMapper {
    public SettlementPeriodEntity toEntity(SettlementPeriod period) {
        SettlementPeriodEntity entity = new SettlementPeriodEntity(
                period.getId(),
                period.getGroupId(),
                period.getStartDate(),
                period.getEndDate(),
                period.getStatus(),
                period.getStrategySnapshot().getStrategyType()
        );
        Set<SettlementPeriodParticipantEntity> participants = new LinkedHashSet<>();
        for (PeriodParticipant participant : period.getParticipants()) {
            participants.add(new SettlementPeriodParticipantEntity(
                    new SettlementPeriodParticipantEntityId(period.getId(), participant.getUserId()),
                    entity
            ));
        }
        entity.replaceParticipants(participants);
        return entity;
    }

    public SettlementPeriod toDomain(SettlementPeriodEntity entity) {
        Set<PeriodParticipant> participants = new LinkedHashSet<>();
        for (SettlementPeriodParticipantEntity participantEntity : entity.getParticipants()) {
            participants.add(new PeriodParticipant(participantEntity.getId().getUserId()));
        }
        return new SettlementPeriod(
                entity.getId(),
                entity.getGroupId(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getStatus(),
                SettlementStrategySnapshot.of(entity.getStrategyType()),
                participants
        );
    }
}
