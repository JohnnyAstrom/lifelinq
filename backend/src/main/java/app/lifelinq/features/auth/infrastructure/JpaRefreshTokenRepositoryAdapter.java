package app.lifelinq.features.auth.infrastructure;

import app.lifelinq.features.auth.domain.RefreshToken;
import app.lifelinq.features.auth.domain.RefreshTokenRepository;
import java.util.Optional;
import java.util.UUID;

public final class JpaRefreshTokenRepositoryAdapter implements RefreshTokenRepository {
    private final JpaRefreshTokenRepository tokenRepository;
    private final JpaRefreshSessionRepository sessionRepository;

    public JpaRefreshTokenRepositoryAdapter(
            JpaRefreshTokenRepository tokenRepository,
            JpaRefreshSessionRepository sessionRepository
    ) {
        if (tokenRepository == null) {
            throw new IllegalArgumentException("tokenRepository must not be null");
        }
        if (sessionRepository == null) {
            throw new IllegalArgumentException("sessionRepository must not be null");
        }
        this.tokenRepository = tokenRepository;
        this.sessionRepository = sessionRepository;
    }

    @Override
    public void save(RefreshToken refreshToken) {
        if (refreshToken == null) {
            throw new IllegalArgumentException("refreshToken must not be null");
        }
        tokenRepository.save(toEntity(refreshToken));
    }

    @Override
    public Optional<RefreshToken> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return tokenRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalArgumentException("tokenHash must not be blank");
        }
        return tokenRepository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    private RefreshTokenEntity toEntity(RefreshToken token) {
        RefreshSessionEntity sessionRef = sessionRepository.getReferenceById(token.getSessionId());
        return new RefreshTokenEntity(
                token.getId(),
                sessionRef,
                token.getTokenHash(),
                token.getIssuedAt(),
                token.getIdleExpiresAt(),
                token.getUsedAt(),
                token.getReplacedByTokenId(),
                token.getRevokedAt(),
                token.getVersion()
        );
    }

    private RefreshToken toDomain(RefreshTokenEntity entity) {
        return new RefreshToken(
                entity.getId(),
                entity.getSession().getId(),
                entity.getTokenHash(),
                entity.getIssuedAt(),
                entity.getIdleExpiresAt(),
                entity.getUsedAt(),
                entity.getReplacedByTokenId(),
                entity.getRevokedAt(),
                entity.getVersion()
        );
    }
}
