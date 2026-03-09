package app.lifelinq.features.economy.infrastructure;

import app.lifelinq.features.economy.domain.SettlementPeriod;
import app.lifelinq.features.economy.domain.SettlementPeriodRepository;
import app.lifelinq.features.economy.domain.SettlementPeriodStatus;
import java.util.Optional;
import java.util.UUID;

public class JpaSettlementPeriodRepositoryAdapter implements SettlementPeriodRepository {
    private final SettlementPeriodJpaRepository settlementPeriodJpaRepository;
    private final SettlementPeriodMapper settlementPeriodMapper;

    public JpaSettlementPeriodRepositoryAdapter(
            SettlementPeriodJpaRepository settlementPeriodJpaRepository,
            SettlementPeriodMapper settlementPeriodMapper
    ) {
        this.settlementPeriodJpaRepository = settlementPeriodJpaRepository;
        this.settlementPeriodMapper = settlementPeriodMapper;
    }

    @Override
    public void save(SettlementPeriod period) {
        settlementPeriodJpaRepository.save(settlementPeriodMapper.toEntity(period));
    }

    @Override
    public Optional<SettlementPeriod> findById(UUID periodId) {
        return settlementPeriodJpaRepository.findById(periodId).map(settlementPeriodMapper::toDomain);
    }

    @Override
    public Optional<SettlementPeriod> findOpenByGroupId(UUID groupId) {
        return settlementPeriodJpaRepository
                .findFirstByGroupIdAndStatus(groupId, SettlementPeriodStatus.OPEN)
                .map(settlementPeriodMapper::toDomain);
    }

    @Override
    public boolean existsOpenByGroupId(UUID groupId) {
        return settlementPeriodJpaRepository.existsByGroupIdAndStatus(groupId, SettlementPeriodStatus.OPEN);
    }
}
