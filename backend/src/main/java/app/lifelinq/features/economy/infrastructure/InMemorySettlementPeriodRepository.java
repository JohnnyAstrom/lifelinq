package app.lifelinq.features.economy.infrastructure;

import app.lifelinq.features.economy.domain.SettlementPeriod;
import app.lifelinq.features.economy.domain.SettlementPeriodRepository;
import app.lifelinq.features.economy.domain.SettlementPeriodStatus;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemorySettlementPeriodRepository implements SettlementPeriodRepository {
    private final Map<UUID, SettlementPeriod> periods = new ConcurrentHashMap<>();

    @Override
    public void save(SettlementPeriod period) {
        periods.put(period.getId(), period);
    }

    @Override
    public void flush() {
        // No-op for in-memory storage.
    }

    @Override
    public Optional<SettlementPeriod> findById(UUID periodId) {
        return Optional.ofNullable(periods.get(periodId));
    }

    @Override
    public Optional<SettlementPeriod> findOpenByGroupId(UUID groupId) {
        return periods.values().stream()
                .filter(period -> groupId.equals(period.getGroupId()))
                .filter(period -> period.getStatus() == SettlementPeriodStatus.OPEN)
                .findFirst();
    }

    @Override
    public boolean existsOpenByGroupId(UUID groupId) {
        return findOpenByGroupId(groupId).isPresent();
    }
}
