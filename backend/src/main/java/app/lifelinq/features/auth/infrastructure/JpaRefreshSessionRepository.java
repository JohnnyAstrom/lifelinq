package app.lifelinq.features.auth.infrastructure;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaRefreshSessionRepository extends JpaRepository<RefreshSessionEntity, UUID> {
}

