package app.lifelinq.features.household.infrastructure;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipJpaRepository extends JpaRepository<MembershipEntity, MembershipEntityId> {
    List<MembershipEntity> findByIdHouseholdId(UUID householdId);

    List<MembershipEntity> findByIdUserId(UUID userId);

    long deleteByIdHouseholdIdAndIdUserId(UUID householdId, UUID userId);
}
