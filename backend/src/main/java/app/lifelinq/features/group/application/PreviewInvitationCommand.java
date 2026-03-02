package app.lifelinq.features.group.application;

import java.time.Instant;

public final class PreviewInvitationCommand {
    private final String token;
    private final Instant now;

    public PreviewInvitationCommand(String token, Instant now) {
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
