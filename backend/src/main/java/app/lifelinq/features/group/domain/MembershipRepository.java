package app.lifelinq.features.group.domain;

import java.util.List;
import java.util.UUID;

public interface MembershipRepository {
    void save(Membership membership);

    List<Membership> findByGroupId(UUID groupId);

    List<Membership> findByUserId(UUID userId);

    List<UUID> findGroupIdsByUserId(UUID userId);

    boolean deleteByGroupIdAndUserId(UUID groupId, UUID userId);

    void deleteByUserId(UUID userId);
}
