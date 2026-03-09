package app.lifelinq.features.economy.api;

import java.util.UUID;

public record CloseSettlementPeriodResponse(
        UUID closedPeriodId,
        UUID newOpenPeriodId
) {
}
