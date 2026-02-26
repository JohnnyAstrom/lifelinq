package app.lifelinq.features.group.contract;

import java.util.UUID;

public record UserGroupMembershipSummary(UUID groupId, String groupName, String role) {
}
