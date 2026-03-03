package app.lifelinq.features.group.application;

import app.lifelinq.features.group.domain.InvitationType;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class CreateInvitationCommand {
    private final UUID groupId;
    private final InvitationType type;
    private final String inviteeEmail;
    private final String inviterDisplayName;
    private final Instant now;
    private final Duration ttl;
    private final Integer maxUses;

    public CreateInvitationCommand(
            UUID groupId,
            InvitationType type,
            String inviteeEmail,
            String inviterDisplayName,
            Instant now,
            Duration ttl,
            Integer maxUses
    ) {
        this.groupId = groupId;
        this.type = type;
        this.inviteeEmail = inviteeEmail;
        this.inviterDisplayName = inviterDisplayName;
        this.now = now;
        this.ttl = ttl;
        this.maxUses = maxUses;
    }

    public CreateInvitationCommand(
            UUID groupId,
            InvitationType type,
            String inviteeEmail,
            Instant now,
            Duration ttl,
            Integer maxUses
    ) {
        this(groupId, type, inviteeEmail, null, now, ttl, maxUses);
    }

    public UUID getGroupId() {
        return groupId;
    }

    public String getInviteeEmail() {
        return inviteeEmail;
    }

    public InvitationType getType() {
        return type;
    }

    public String getInviterDisplayName() {
        return inviterDisplayName;
    }

    public Instant getNow() {
        return now;
    }

    public Duration getTtl() {
        return ttl;
    }

    public Integer getMaxUses() {
        return maxUses;
    }
}
