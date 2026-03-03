package app.lifelinq.features.auth.domain;

import java.time.Instant;
import java.util.UUID;

public final class MagicLinkChallenge {
    private final UUID id;
    private final String token;
    private final String email;
    private final Instant expiresAt;
    private final Instant consumedAt;
    private final Long version;

    public MagicLinkChallenge(
            UUID id,
            String token,
            String email,
            Instant expiresAt,
            Instant consumedAt
    ) {
        this(id, token, email, expiresAt, consumedAt, null);
    }

    public MagicLinkChallenge(
            UUID id,
            String token,
            String email,
            Instant expiresAt,
            Instant consumedAt,
            Long version
    ) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("expiresAt must not be null");
        }
        this.id = id;
        this.token = token;
        this.email = email;
        this.expiresAt = expiresAt;
        this.consumedAt = consumedAt;
        this.version = version;
    }

    public UUID getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getConsumedAt() {
        return consumedAt;
    }

    public Long getVersion() {
        return version;
    }

    public boolean isExpired(Instant now) {
        if (now == null) {
            throw new IllegalArgumentException("now must not be null");
        }
        return now.isAfter(expiresAt);
    }

    public boolean isConsumed() {
        return consumedAt != null;
    }

    public MagicLinkChallenge consume(Instant now) {
        if (now == null) {
            throw new IllegalArgumentException("now must not be null");
        }
        if (isConsumed()) {
            throw new IllegalStateException("challenge already consumed");
        }
        return new MagicLinkChallenge(id, token, email, expiresAt, now, version);
    }
}
