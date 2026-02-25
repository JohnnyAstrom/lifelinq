package app.lifelinq.features.group.contract;

import java.util.List;
import java.util.UUID;

public interface GroupAccountDeletionGovernancePort {
    List<UserGroupMembershipView> findMembershipsForUser(UUID userId);

    void deleteMembershipsByUserId(UUID userId);

    void deleteEmptyGroupsByIds(List<UUID> groupIds);
}
