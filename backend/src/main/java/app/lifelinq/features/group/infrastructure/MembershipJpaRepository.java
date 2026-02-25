package app.lifelinq.features.group.infrastructure;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipJpaRepository extends JpaRepository<MembershipEntity, MembershipEntityId> {
    List<MembershipEntity> findByIdGroupId(UUID groupId);

    List<MembershipEntity> findByIdUserId(UUID userId);

    long deleteByIdGroupIdAndIdUserId(UUID groupId, UUID userId);

    long deleteByIdUserId(UUID userId);
}
