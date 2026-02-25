package app.lifelinq.features.group.api;

import java.time.Instant;
import java.util.UUID;

public final class CreateInvitationResponse {
    private final UUID invitationId;
    private final String token;
    private final Instant expiresAt;

    public CreateInvitationResponse(UUID invitationId, String token, Instant expiresAt) {
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
