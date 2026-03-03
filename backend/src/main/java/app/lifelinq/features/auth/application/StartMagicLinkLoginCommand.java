package app.lifelinq.features.auth.application;

import java.time.Duration;
import java.time.Instant;

public final class StartMagicLinkLoginCommand {
    private final String email;
    private final Instant now;
    private final Duration ttl;
    private final String verifyBaseUrl;

    public StartMagicLinkLoginCommand(String email, Instant now, Duration ttl, String verifyBaseUrl) {
        this.email = email;
        this.now = now;
        this.ttl = ttl;
        this.verifyBaseUrl = verifyBaseUrl;
    }

    public String getEmail() {
        return email;
    }

    public Instant getNow() {
        return now;
    }

    public Duration getTtl() {
        return ttl;
    }

    public String getVerifyBaseUrl() {
        return verifyBaseUrl;
    }
}

