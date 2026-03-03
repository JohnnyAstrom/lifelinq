package app.lifelinq.features.auth.infrastructure;

import app.lifelinq.features.auth.domain.RefreshSession;
import app.lifelinq.features.auth.domain.RefreshSessionRepository;
import java.util.Optional;
import java.util.UUID;

public final class JpaRefreshSessionRepositoryAdapter implements RefreshSessionRepository {
    private final JpaRefreshSessionRepository repository;

    public JpaRefreshSessionRepositoryAdapter(JpaRefreshSessionRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("repository must not be null");
        }
        this.repository = repository;
    }

    @Override
    public void save(RefreshSession refreshSession) {
        if (refreshSession == null) {
            throw new IllegalArgumentException("refreshSession must not be null");
        }
        repository.save(toEntity(refreshSession));
    }

    @Override
    public Optional<RefreshSession> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return repository.findById(id).map(this::toDomain);
    }

    private RefreshSessionEntity toEntity(RefreshSession session) {
        return new RefreshSessionEntity(
                session.getId(),
                session.getUserId(),
                session.getCreatedAt(),
                session.getAbsoluteExpiresAt(),
                session.getRevokedAt(),
                session.getRevokeReason(),
                session.getVersion()
        );
    }

    private RefreshSession toDomain(RefreshSessionEntity entity) {
        return new RefreshSession(
                entity.getId(),
                entity.getUserId(),
                entity.getCreatedAt(),
                entity.getAbsoluteExpiresAt(),
                entity.getRevokedAt(),
                entity.getRevokeReason(),
                entity.getVersion()
        );
    }
}
