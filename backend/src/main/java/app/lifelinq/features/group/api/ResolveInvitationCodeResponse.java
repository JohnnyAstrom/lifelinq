package app.lifelinq.features.group.api;

import app.lifelinq.features.group.domain.InvitationStatus;
import app.lifelinq.features.group.domain.InvitationType;
import java.time.Instant;
import java.util.UUID;

public record ResolveInvitationCodeResponse(
        UUID invitationId,
        UUID groupId,
        String token,
        InvitationType type,
        InvitationStatus status,
        Instant expiresAt
) {
}
