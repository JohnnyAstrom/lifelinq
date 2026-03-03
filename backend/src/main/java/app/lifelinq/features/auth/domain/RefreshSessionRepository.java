package app.lifelinq.features.auth.domain;

import java.util.Optional;
import java.util.UUID;

public interface RefreshSessionRepository {
    void save(RefreshSession refreshSession);

    Optional<RefreshSession> findById(UUID id);
}
