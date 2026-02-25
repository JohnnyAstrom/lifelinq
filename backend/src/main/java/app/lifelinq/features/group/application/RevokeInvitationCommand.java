package app.lifelinq.features.group.application;

import java.time.Instant;
import java.util.UUID;

public final class RevokeInvitationCommand {
    private final UUID invitationId;
    private final Instant now;

    public RevokeInvitationCommand(UUID invitationId, Instant now) {
        this.invitationId = invitationId;
        this.now = now;
    }

    public UUID getInvitationId() {
        return invitationId;
    }

    public Instant getNow() {
        return now;
    }
}
