package app.lifelinq.features.household.application;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class CreateInvitationCommand {
    private final UUID householdId;
    private final String inviteeEmail;
    private final Instant now;
    private final Duration ttl;

    public CreateInvitationCommand(UUID householdId, String inviteeEmail, Instant now, Duration ttl) {
        this.householdId = householdId;
        this.inviteeEmail = inviteeEmail;
        this.now = now;
        this.ttl = ttl;
    }

    public UUID getHouseholdId() {
        return householdId;
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
