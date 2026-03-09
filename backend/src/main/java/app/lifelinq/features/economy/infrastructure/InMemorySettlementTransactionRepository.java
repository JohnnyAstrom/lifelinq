package app.lifelinq.features.economy.infrastructure;

import app.lifelinq.features.economy.domain.SettlementTransaction;
import app.lifelinq.features.economy.domain.SettlementTransactionRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemorySettlementTransactionRepository implements SettlementTransactionRepository {
    private final Map<UUID, SettlementTransaction> transactions = new ConcurrentHashMap<>();

    @Override
    public void save(SettlementTransaction transaction) {
        transactions.put(transaction.getId(), transaction);
    }

    @Override
    public Optional<SettlementTransaction> findById(UUID transactionId) {
        return Optional.ofNullable(transactions.get(transactionId));
    }

    @Override
    public List<SettlementTransaction> findByPeriodId(UUID periodId) {
        return transactions.values().stream()
                .filter(transaction -> periodId.equals(transaction.getPeriodId()))
                .sorted(Comparator.comparing(SettlementTransaction::getCreatedAt))
                .toList();
    }

    @Override
    public List<SettlementTransaction> findActiveByPeriodId(UUID periodId) {
        return transactions.values().stream()
                .filter(transaction -> periodId.equals(transaction.getPeriodId()))
                .filter(transaction -> transaction.getDeletedAt() == null)
                .sorted(Comparator.comparing(SettlementTransaction::getCreatedAt))
                .toList();
    }
}
