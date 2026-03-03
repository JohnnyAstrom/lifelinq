package app.lifelinq.features.auth.infrastructure;

import app.lifelinq.features.auth.domain.AuthIdentity;
import app.lifelinq.features.auth.domain.AuthIdentityRepository;
import app.lifelinq.features.auth.domain.AuthProvider;
import java.util.Optional;

public final class JpaAuthIdentityRepositoryAdapter implements AuthIdentityRepository {
    private final AuthIdentityJpaRepository repository;
    private final AuthIdentityMapper mapper;

    public JpaAuthIdentityRepositoryAdapter(AuthIdentityJpaRepository repository, AuthIdentityMapper mapper) {
        if (repository == null) {
            throw new IllegalArgumentException("repository must not be null");
        }
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<AuthIdentity> findByProviderAndSubject(AuthProvider provider, String subject) {
        if (provider == null) {
            throw new IllegalArgumentException("provider must not be null");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("subject must not be blank");
        }
        return repository.findByProviderAndSubject(provider, subject).map(mapper::toDomain);
    }

    @Override
    public void save(AuthIdentity identity) {
        if (identity == null) {
            throw new IllegalArgumentException("identity must not be null");
        }
        repository.save(mapper.toEntity(identity));
    }
}

