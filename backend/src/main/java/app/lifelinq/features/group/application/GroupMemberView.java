package app.lifelinq.features.group.application;

import app.lifelinq.features.group.domain.GroupRole;
import java.util.UUID;

public record GroupMemberView(
        UUID userId,
        GroupRole role,
        String displayName
) {
}
