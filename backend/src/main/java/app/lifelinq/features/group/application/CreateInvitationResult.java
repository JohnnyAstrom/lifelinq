package app.lifelinq.features.group.application;

import java.time.Instant;
import java.util.UUID;

public final class CreateInvitationResult {
    private final UUID invitationId;
    private final String token;
    private final Instant expiresAt;

    public CreateInvitationResult(UUID invitationId, String token, Instant expiresAt) {
        this.invitationId = invitationId;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public UUID getInvitationId() {
        return invitationId;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
