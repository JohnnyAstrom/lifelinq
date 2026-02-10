package app.lifelinq.features.household.application;

import java.time.Instant;

public final class RevokeInvitationCommand {
    private final String token;
    private final Instant now;

    public RevokeInvitationCommand(String token, Instant now) {
        this.token = token;
        this.now = now;
    }

    public String getToken() {
        return token;
    }

    public Instant getNow() {
        return now;
    }
}
