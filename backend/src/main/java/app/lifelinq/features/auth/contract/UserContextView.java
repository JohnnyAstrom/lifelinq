package app.lifelinq.features.auth.contract;

import java.util.List;
import java.util.UUID;

public record UserContextView(
        UUID userId,
        UUID activeGroupId,
        List<UserMembershipView> memberships
) {
}
