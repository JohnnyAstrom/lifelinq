package app.lifelinq.features.economy.application;

import app.lifelinq.features.economy.domain.ParticipantBalance;
import app.lifelinq.features.economy.domain.RecommendedPayment;
import java.util.List;

public record CalculateSettlementResult(
        List<ParticipantBalance> balances,
        List<RecommendedPayment> recommendedPayments
) {
}
