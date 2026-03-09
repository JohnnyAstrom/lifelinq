package app.lifelinq.features.economy.application;

import java.util.UUID;

public record CloseSettlementPeriodResult(
        UUID closedPeriodId,
        UUID newOpenPeriodId
) {
}
