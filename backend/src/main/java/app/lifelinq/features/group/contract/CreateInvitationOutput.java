package app.lifelinq.features.group.contract;

import java.time.Instant;
import java.util.UUID;

public record CreateInvitationOutput(
        UUID invitationId,
        String token,
        String shortCode,
        Instant expiresAt
) {
}
