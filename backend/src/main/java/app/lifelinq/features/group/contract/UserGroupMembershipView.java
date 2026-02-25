package app.lifelinq.features.group.contract;

import java.util.UUID;

public record UserGroupMembershipView(
        UUID groupId,
        boolean admin,
        int memberCount,
        int adminCount
) {
}
