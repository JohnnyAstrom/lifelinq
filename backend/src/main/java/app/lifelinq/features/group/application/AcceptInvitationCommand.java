package app.lifelinq.features.group.application;

import java.time.Instant;
import java.util.UUID;

public final class AcceptInvitationCommand {
    private final String token;
    private final UUID userId;
    private final Instant now;

    public AcceptInvitationCommand(String token, UUID userId, Instant now) {
        this.token = token;
        this.userId = userId;
        this.now = now;
    }

    public String getToken() {
        return token;
    }

    public UUID getUserId() {
        return userId;
    }

    public Instant getNow() {
        return now;
    }
}
