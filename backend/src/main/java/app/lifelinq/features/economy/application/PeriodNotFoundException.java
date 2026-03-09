package app.lifelinq.features.economy.application;

import java.util.UUID;

public final class PeriodNotFoundException extends RuntimeException {
    public PeriodNotFoundException(UUID periodId) {
        super("period not found: " + periodId);
    }
}
