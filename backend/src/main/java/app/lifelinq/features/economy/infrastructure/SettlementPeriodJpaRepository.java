package app.lifelinq.features.economy.infrastructure;

import app.lifelinq.features.economy.domain.SettlementPeriodStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementPeriodJpaRepository extends JpaRepository<SettlementPeriodEntity, UUID> {
    Optional<SettlementPeriodEntity> findFirstByGroupIdAndStatus(UUID groupId, SettlementPeriodStatus status);

    boolean existsByGroupIdAndStatus(UUID groupId, SettlementPeriodStatus status);
}
