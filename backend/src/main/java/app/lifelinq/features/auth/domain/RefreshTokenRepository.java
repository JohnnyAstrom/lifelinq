package app.lifelinq.features.auth.domain;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {
    void save(RefreshToken refreshToken);

    Optional<RefreshToken> findById(UUID id);

    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
