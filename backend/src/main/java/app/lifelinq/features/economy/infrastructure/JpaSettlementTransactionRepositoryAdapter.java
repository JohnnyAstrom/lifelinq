package app.lifelinq.features.economy.infrastructure;

import app.lifelinq.features.economy.domain.SettlementTransaction;
import app.lifelinq.features.economy.domain.SettlementTransactionRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaSettlementTransactionRepositoryAdapter implements SettlementTransactionRepository {
    private final SettlementTransactionJpaRepository settlementTransactionJpaRepository;
    private final SettlementTransactionMapper settlementTransactionMapper;

    public JpaSettlementTransactionRepositoryAdapter(
            SettlementTransactionJpaRepository settlementTransactionJpaRepository,
            SettlementTransactionMapper settlementTransactionMapper
    ) {
        this.settlementTransactionJpaRepository = settlementTransactionJpaRepository;
        this.settlementTransactionMapper = settlementTransactionMapper;
    }

    @Override
    public void save(SettlementTransaction transaction) {
        settlementTransactionJpaRepository.save(settlementTransactionMapper.toEntity(transaction));
    }

    @Override
    public Optional<SettlementTransaction> findById(UUID transactionId) {
        return settlementTransactionJpaRepository.findById(transactionId).map(settlementTransactionMapper::toDomain);
    }

    @Override
    public List<SettlementTransaction> findByPeriodId(UUID periodId) {
        return settlementTransactionJpaRepository.findByPeriodIdOrderByCreatedAtAsc(periodId)
                .stream()
                .map(settlementTransactionMapper::toDomain)
                .toList();
    }

    @Override
    public List<SettlementTransaction> findActiveByPeriodId(UUID periodId) {
        return settlementTransactionJpaRepository.findByPeriodIdAndDeletedAtIsNullOrderByCreatedAtAsc(periodId)
                .stream()
                .map(settlementTransactionMapper::toDomain)
                .toList();
    }
}
