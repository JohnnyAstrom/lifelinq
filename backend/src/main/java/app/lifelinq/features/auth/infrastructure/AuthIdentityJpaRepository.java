package app.lifelinq.features.auth.infrastructure;

import app.lifelinq.features.auth.domain.AuthProvider;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthIdentityJpaRepository extends JpaRepository<AuthIdentityEntity, UUID> {
    Optional<AuthIdentityEntity> findByProviderAndSubject(AuthProvider provider, String subject);
}

