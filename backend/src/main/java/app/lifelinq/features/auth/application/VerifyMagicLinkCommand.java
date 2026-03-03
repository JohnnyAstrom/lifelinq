package app.lifelinq.features.auth.application;

import java.time.Instant;

public final class VerifyMagicLinkCommand {
    private final String token;
    private final Instant now;

    public VerifyMagicLinkCommand(String token, Instant now) {
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

