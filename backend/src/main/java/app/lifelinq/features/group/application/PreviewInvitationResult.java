package app.lifelinq.features.group.application;

import app.lifelinq.features.group.domain.InvitationType;
import java.time.Instant;

public final class PreviewInvitationResult {
    private final boolean valid;
    private final PreviewInvitationReason reason;
    private final String placeName;
    private final String inviterDisplayName;
    private final Instant expiresAt;
    private final InvitationType type;

    public PreviewInvitationResult(
            boolean valid,
            PreviewInvitationReason reason,
            String placeName,
            String inviterDisplayName,
            Instant expiresAt,
            InvitationType type
    ) {
        this.valid = valid;
        this.reason = reason;
        this.placeName = placeName;
        this.inviterDisplayName = inviterDisplayName;
        this.expiresAt = expiresAt;
        this.type = type;
    }

    public PreviewInvitationResult(
            boolean valid,
            PreviewInvitationReason reason,
            String placeName,
            Instant expiresAt,
            InvitationType type
    ) {
        this(valid, reason, placeName, null, expiresAt, type);
    }

    public boolean isValid() {
        return valid;
    }

    public PreviewInvitationReason getReason() {
        return reason;
    }

    public String getPlaceName() {
        return placeName;
    }

    public String getInviterDisplayName() {
        return inviterDisplayName;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public InvitationType getType() {
        return type;
    }
}
