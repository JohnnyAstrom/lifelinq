package app.lifelinq.features.economy.api;

import app.lifelinq.features.economy.domain.SettlementPeriodStatus;
import app.lifelinq.features.economy.domain.SettlementStrategyType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ActiveSettlementPeriodResponse(
        UUID periodId,
        UUID groupId,
        Instant startDate,
        Instant endDate,
        SettlementPeriodStatus status,
        SettlementStrategyType strategyType,
        Map<UUID, BigDecimal> percentageShares,
        List<UUID> participantUserIds
) {
}
