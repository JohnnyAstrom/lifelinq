package app.lifelinq.features.group.application;

import app.lifelinq.features.group.domain.InvitationStatus;
import app.lifelinq.features.group.domain.InvitationType;
import java.time.Instant;
import java.util.UUID;

public record GroupInvitationView(
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

