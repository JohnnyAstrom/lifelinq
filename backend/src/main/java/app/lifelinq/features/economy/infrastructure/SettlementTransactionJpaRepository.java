package app.lifelinq.features.economy.infrastructure;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementTransactionJpaRepository extends JpaRepository<SettlementTransactionEntity, UUID> {
    List<SettlementTransactionEntity> findByPeriodIdOrderByCreatedAtAsc(UUID periodId);

    List<SettlementTransactionEntity> findByPeriodIdAndDeletedAtIsNullOrderByCreatedAtAsc(UUID periodId);
}
