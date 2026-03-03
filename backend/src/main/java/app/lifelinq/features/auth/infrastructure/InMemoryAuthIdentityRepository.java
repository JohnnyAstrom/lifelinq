package app.lifelinq.features.auth.infrastructure;

import app.lifelinq.features.auth.domain.AuthIdentity;
import app.lifelinq.features.auth.domain.AuthIdentityRepository;
import app.lifelinq.features.auth.domain.AuthProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryAuthIdentityRepository implements AuthIdentityRepository {
    private final Map<String, AuthIdentity> identities = new HashMap<>();

    @Override
    public Optional<AuthIdentity> findByProviderAndSubject(AuthProvider provider, String subject) {
        if (provider == null) {
            throw new IllegalArgumentException("provider must not be null");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("subject must not be blank");
        }
        return Optional.ofNullable(identities.get(key(provider, subject)));
    }

    @Override
    public void save(AuthIdentity identity) {
        if (identity == null) {
            throw new IllegalArgumentException("identity must not be null");
        }
        identities.put(key(identity.getProvider(), identity.getSubject()), identity);
    }

    private String key(AuthProvider provider, String subject) {
        return provider.name() + ":" + subject;
    }
}

