package app.lifelinq.features.economy.infrastructure;

import app.lifelinq.features.economy.domain.PeriodParticipant;
import app.lifelinq.features.economy.domain.SettlementPeriod;
import app.lifelinq.features.economy.domain.SettlementStrategyType;
import app.lifelinq.features.economy.domain.SettlementStrategySnapshot;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SettlementPeriodMapper {
    private final ObjectMapper objectMapper;

    public SettlementPeriodMapper(ObjectMapper objectMapper) {
        if (objectMapper == null) {
            throw new IllegalArgumentException("objectMapper must not be null");
        }
        this.objectMapper = objectMapper.copy().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public SettlementPeriodEntity toEntity(SettlementPeriod period) {
        SettlementPeriodEntity entity = new SettlementPeriodEntity(
                period.getId(),
                period.getGroupId(),
                period.getStartDate(),
                period.getEndDate(),
                period.getStatus(),
                period.getStrategySnapshot().getStrategyType(),
                serializeStrategySnapshot(period.getStrategySnapshot())
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
                deserializeStrategySnapshot(entity.getStrategySnapshotJson(), entity.getStrategyType()),
                participants
        );
    }

    private String serializeStrategySnapshot(SettlementStrategySnapshot snapshot) {
        try {
            return objectMapper.writeValueAsString(new StrategySnapshotJson(
                    snapshot.getStrategyType(),
                    snapshot.getPercentageShares()
            ));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("failed to serialize settlement strategy snapshot", ex);
        }
    }

    private SettlementStrategySnapshot deserializeStrategySnapshot(
            String strategySnapshotJson,
            SettlementStrategyType fallbackType
    ) {
        if (strategySnapshotJson == null || strategySnapshotJson.isBlank()) {
            return SettlementStrategySnapshot.of(fallbackType);
        }
        try {
            StrategySnapshotJson payload = objectMapper.readValue(strategySnapshotJson, StrategySnapshotJson.class);
            if (payload.strategyType == SettlementStrategyType.PERCENTAGE_COST) {
                Map<UUID, BigDecimal> shares = payload.percentageShares == null ? Map.of() : payload.percentageShares;
                return SettlementStrategySnapshot.percentageCost(shares);
            }
            return SettlementStrategySnapshot.of(payload.strategyType == null ? fallbackType : payload.strategyType);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("failed to deserialize settlement strategy snapshot", ex);
        }
    }

    static final class StrategySnapshotJson {
        public SettlementStrategyType strategyType;
        public Map<UUID, BigDecimal> percentageShares;

        StrategySnapshotJson() {
        }

        StrategySnapshotJson(SettlementStrategyType strategyType, Map<UUID, BigDecimal> percentageShares) {
            this.strategyType = strategyType;
            this.percentageShares = percentageShares;
        }
    }
}
