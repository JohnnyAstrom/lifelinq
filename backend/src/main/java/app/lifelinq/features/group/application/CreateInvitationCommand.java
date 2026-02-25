package app.lifelinq.features.group.application;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class CreateInvitationCommand {
    private final UUID groupId;
    private final String inviteeEmail;
    private final Instant now;
    private final Duration ttl;

    public CreateInvitationCommand(UUID groupId, String inviteeEmail, Instant now, Duration ttl) {
        this.groupId = groupId;
        this.inviteeEmail = inviteeEmail;
        this.now = now;
        this.ttl = ttl;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public String getInviteeEmail() {
        return inviteeEmail;
    }

    public Instant getNow() {
        return now;
    }

    public Duration getTtl() {
        return ttl;
    }
}
