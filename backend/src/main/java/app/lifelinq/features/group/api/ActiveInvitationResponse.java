package app.lifelinq.features.group.api;

import java.time.Instant;
import java.util.UUID;

public final class ActiveInvitationResponse {
    private final UUID invitationId;
    private final String token;
    private final String shortCode;
    private final Instant expiresAt;

    public ActiveInvitationResponse(UUID invitationId, String token, String shortCode, Instant expiresAt) {
        this.invitationId = invitationId;
        this.token = token;
        this.shortCode = shortCode;
        this.expiresAt = expiresAt;
    }

    public static ActiveInvitationResponse empty() {
        return new ActiveInvitationResponse(null, null, null, null);
    }

    public UUID getInvitationId() {
        return invitationId;
    }

    public String getToken() {
        return token;
    }

    public String getShortCode() {
        return shortCode;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
