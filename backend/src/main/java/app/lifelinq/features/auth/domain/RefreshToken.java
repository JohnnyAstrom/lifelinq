package app.lifelinq.features.auth.domain;

import java.time.Instant;
import java.util.UUID;

public final class RefreshToken {
    private final UUID id;
    private final UUID sessionId;
    private final String tokenHash;
    private final Instant issuedAt;
    private final Instant idleExpiresAt;
    private final Instant usedAt;
    private final UUID replacedByTokenId;
    private final Instant revokedAt;
    private final Integer version;

    public RefreshToken(
            UUID id,
            UUID sessionId,
            String tokenHash,
            Instant issuedAt,
            Instant idleExpiresAt,
            Instant usedAt,
            UUID replacedByTokenId,
            Instant revokedAt
    ) {
        this(id, sessionId, tokenHash, issuedAt, idleExpiresAt, usedAt, replacedByTokenId, revokedAt, null);
    }

    public RefreshToken(
            UUID id,
            UUID sessionId,
            String tokenHash,
            Instant issuedAt,
            Instant idleExpiresAt,
            Instant usedAt,
            UUID replacedByTokenId,
            Instant revokedAt,
            Integer version
    ) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (sessionId == null) {
            throw new IllegalArgumentException("sessionId must not be null");
        }
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalArgumentException("tokenHash must not be blank");
        }
        if (issuedAt == null) {
            throw new IllegalArgumentException("issuedAt must not be null");
        }
        if (idleExpiresAt == null) {
            throw new IllegalArgumentException("idleExpiresAt must not be null");
        }
        if (idleExpiresAt.isBefore(issuedAt)) {
            throw new IllegalArgumentException("idleExpiresAt must not be before issuedAt");
        }
        if (replacedByTokenId != null && usedAt == null) {
            throw new IllegalArgumentException("usedAt must not be null when replacedByTokenId is set");
        }

        this.id = id;
        this.sessionId = sessionId;
        this.tokenHash = tokenHash;
        this.issuedAt = issuedAt;
        this.idleExpiresAt = idleExpiresAt;
        this.usedAt = usedAt;
        this.replacedByTokenId = replacedByTokenId;
        this.revokedAt = revokedAt;
        this.version = version;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public Instant getIdleExpiresAt() {
        return idleExpiresAt;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    public UUID getReplacedByTokenId() {
        return replacedByTokenId;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public Integer getVersion() {
        return version;
    }

    public boolean isExpired(Instant now) {
        if (now == null) {
            throw new IllegalArgumentException("now must not be null");
        }
        return now.isAfter(idleExpiresAt);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public RefreshToken replaceWith(UUID newTokenId, Instant now) {
        if (newTokenId == null) {
            throw new IllegalArgumentException("newTokenId must not be null");
        }
        if (now == null) {
            throw new IllegalArgumentException("now must not be null");
        }
        if (newTokenId.equals(id)) {
            throw new IllegalArgumentException("newTokenId must not equal current token id");
        }
        if (isExpired(now)) {
            throw new IllegalStateException("refresh token is expired");
        }
        if (isRevoked()) {
            throw new IllegalStateException("refresh token is revoked");
        }
        if (isUsed()) {
            throw new IllegalStateException("refresh token is already used");
        }
        return new RefreshToken(id, sessionId, tokenHash, issuedAt, idleExpiresAt, now, newTokenId, revokedAt, version);
    }

    public RefreshToken revoke(Instant now) {
        if (now == null) {
            throw new IllegalArgumentException("now must not be null");
        }
        if (isRevoked()) {
            throw new IllegalStateException("refresh token is already revoked");
        }
        return new RefreshToken(id, sessionId, tokenHash, issuedAt, idleExpiresAt, usedAt, replacedByTokenId, now, version);
    }
}
