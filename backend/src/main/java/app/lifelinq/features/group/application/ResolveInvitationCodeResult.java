package app.lifelinq.features.group.application;

import app.lifelinq.features.group.domain.InvitationStatus;
import app.lifelinq.features.group.domain.InvitationType;
import java.time.Instant;
import java.util.UUID;

public record ResolveInvitationCodeResult(
        UUID invitationId,
        UUID groupId,
        InvitationType type,
        InvitationStatus status,
        Instant expiresAt
) {
}
