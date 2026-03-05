package app.lifelinq.features.group.api;

import app.lifelinq.features.group.application.InvitationEffectiveState;
import app.lifelinq.features.group.domain.InvitationStatus;
import app.lifelinq.features.group.domain.InvitationType;
import java.time.Instant;
import java.util.UUID;

public record InvitationListItemResponse(
        UUID invitationId,
        InvitationType type,
        InvitationStatus status,
        InvitationEffectiveState effectiveState,
        String inviteeEmail,
        String inviterDisplayName,
        String token,
        String shortCode,
        Instant expiresAt,
        Integer maxUses,
        int usageCount,
        boolean acceptAllowed
) {
}

