package app.lifelinq.features.economy.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class SettlementMath {
    static final int SCALE = 4;
    static final BigDecimal UNIT = new BigDecimal("0.0001");

    private SettlementMath() {
    }

    static BigDecimal normalize(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        }
        return amount.setScale(SCALE, RoundingMode.HALF_UP);
    }
}
