package app.lifelinq.features.auth.infrastructure;

import app.lifelinq.features.auth.domain.AuthProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "auth_identities")
public class AuthIdentityEntity {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Column(nullable = false)
    private String subject;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    protected AuthIdentityEntity() {
    }

    public AuthIdentityEntity(UUID id, AuthProvider provider, String subject, UUID userId) {
        this.id = id;
        this.provider = provider;
        this.subject = subject;
        this.userId = userId;
    }

    public UUID getId() {
        return id;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public String getSubject() {
        return subject;
    }

    public UUID getUserId() {
        return userId;
    }
}

