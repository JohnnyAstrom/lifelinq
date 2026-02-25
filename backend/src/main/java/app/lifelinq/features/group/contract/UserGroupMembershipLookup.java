package app.lifelinq.features.group.contract;

import java.util.List;
import java.util.UUID;

public interface UserGroupMembershipLookup {
    boolean isMember(UUID userId, UUID groupId);

    List<UserGroupMembershipSummary> listMemberships(UUID userId);
}
