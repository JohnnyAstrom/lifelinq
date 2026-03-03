package app.lifelinq.features.auth.domain;

import java.time.Instant;
import java.util.UUID;

public final class RefreshSession {
    private final UUID id;
    private final UUID userId;
    private final Instant createdAt;
    private final Instant absoluteExpiresAt;
    private final Instant revokedAt;
    private final String revokeReason;
    private final Integer version;

    public RefreshSession(
            UUID id,
            UUID userId,
            Instant createdAt,
            Instant absoluteExpiresAt,
            Instant revokedAt,
            String revokeReason
    ) {
        this(id, userId, createdAt, absoluteExpiresAt, revokedAt, revokeReason, null);
    }

    public RefreshSession(
            UUID id,
            UUID userId,
            Instant createdAt,
            Instant absoluteExpiresAt,
            Instant revokedAt,
            String revokeReason,
            Integer version
    ) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }
        if (absoluteExpiresAt == null) {
            throw new IllegalArgumentException("absoluteExpiresAt must not be null");
        }
        if (absoluteExpiresAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("absoluteExpiresAt must not be before createdAt");
        }
        if (revokedAt == null && revokeReason != null) {
            throw new IllegalArgumentException("revokeReason must be null when session is not revoked");
        }
        if (revokedAt != null && (revokeReason == null || revokeReason.isBlank())) {
            throw new IllegalArgumentException("revokeReason must not be blank when session is revoked");
        }

        this.id = id;
        this.userId = userId;
        this.createdAt = createdAt;
        this.absoluteExpiresAt = absoluteExpiresAt;
        this.revokedAt = revokedAt;
        this.revokeReason = revokeReason;
        this.version = version;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getAbsoluteExpiresAt() {
        return absoluteExpiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public String getRevokeReason() {
        return revokeReason;
    }

    public Integer getVersion() {
        return version;
    }

    public boolean isExpired(Instant now) {
        if (now == null) {
            throw new IllegalArgumentException("now must not be null");
        }
        return now.isAfter(absoluteExpiresAt);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public RefreshSession revoke(String reason, Instant now) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be blank");
        }
        if (now == null) {
            throw new IllegalArgumentException("now must not be null");
        }
        if (isExpired(now)) {
            throw new IllegalStateException("refresh session is expired");
        }
        if (isRevoked()) {
            throw new IllegalStateException("refresh session is already revoked");
        }
        return new RefreshSession(id, userId, createdAt, absoluteExpiresAt, now, reason, version);
    }
}
