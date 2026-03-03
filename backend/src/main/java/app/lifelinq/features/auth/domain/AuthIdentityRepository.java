package app.lifelinq.features.auth.domain;

import java.util.Optional;

public interface AuthIdentityRepository {
    Optional<AuthIdentity> findByProviderAndSubject(AuthProvider provider, String subject);

    void save(AuthIdentity identity);
}

