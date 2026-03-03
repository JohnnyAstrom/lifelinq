package app.lifelinq.features.auth.domain;

import java.util.UUID;

public final class AuthIdentity {
    private final UUID id;
    private final AuthProvider provider;
    private final String subject;
    private final UUID userId;

    public AuthIdentity(UUID id, AuthProvider provider, String subject, UUID userId) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (provider == null) {
            throw new IllegalArgumentException("provider must not be null");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("subject must not be blank");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
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

