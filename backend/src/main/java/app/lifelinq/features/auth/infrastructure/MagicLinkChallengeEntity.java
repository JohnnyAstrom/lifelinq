package app.lifelinq.features.auth.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_magic_link_challenges")
public class MagicLinkChallengeEntity {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String email;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    protected MagicLinkChallengeEntity() {
    }

    public MagicLinkChallengeEntity(
            UUID id,
            String token,
            String email,
            Instant expiresAt,
            Instant consumedAt,
            Long version
    ) {
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
}
