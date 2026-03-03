package app.lifelinq.features.auth.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_refresh_tokens")
public class RefreshTokenEntity {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private RefreshSessionEntity session;

    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "idle_expires_at", nullable = false)
    private Instant idleExpiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "replaced_by_token_id")
    private UUID replacedByTokenId;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Version
    @Column(nullable = false)
    private Integer version;

    protected RefreshTokenEntity() {
    }

    public RefreshTokenEntity(
            UUID id,
            RefreshSessionEntity session,
            String tokenHash,
            Instant issuedAt,
            Instant idleExpiresAt,
            Instant usedAt,
            UUID replacedByTokenId,
            Instant revokedAt,
            Integer version
    ) {
        this.id = id;
        this.session = session;
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

    public RefreshSessionEntity getSession() {
        return session;
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
}

