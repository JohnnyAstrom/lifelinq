package app.lifelinq.features.auth.infrastructure;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MagicLinkChallengeJpaRepository extends JpaRepository<MagicLinkChallengeEntity, UUID> {
    Optional<MagicLinkChallengeEntity> findByToken(String token);

    boolean existsByToken(String token);
}

