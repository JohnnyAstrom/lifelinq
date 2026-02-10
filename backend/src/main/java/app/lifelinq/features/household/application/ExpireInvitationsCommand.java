package app.lifelinq.features.household.application;

import java.time.Instant;

public final class ExpireInvitationsCommand {
    private final Instant now;

    public ExpireInvitationsCommand(Instant now) {
        this.now = now;
    }

    public Instant getNow() {
        return now;
    }
}
