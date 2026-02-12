package app.lifelinq.features.household.contract;

import java.time.Instant;
import java.util.UUID;

public record CreateInvitationOutput(
        UUID invitationId,
        String token,
        Instant expiresAt
) {
}
