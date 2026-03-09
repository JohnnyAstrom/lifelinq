package app.lifelinq.features.economy.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface SettlementCalculator {
    Map<UUID, BigDecimal> calculate(SettlementPeriod period, List<SettlementTransaction> transactions);
}
