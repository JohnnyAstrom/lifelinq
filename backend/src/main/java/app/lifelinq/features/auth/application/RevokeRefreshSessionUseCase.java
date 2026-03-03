package app.lifelinq.features.auth.application;

import app.lifelinq.features.auth.domain.RefreshSession;
import app.lifelinq.features.auth.domain.RefreshSessionRepository;
import app.lifelinq.features.auth.domain.RefreshToken;
import app.lifelinq.features.auth.domain.RefreshTokenHasher;
import app.lifelinq.features.auth.domain.RefreshTokenRepository;
import java.time.Instant;

final class RevokeRefreshSessionUseCase {
    private static final String LOGOUT_REASON = "LOGOUT";

    private final RefreshSessionRepository refreshSessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenHasher refreshTokenHasher;

    RevokeRefreshSessionUseCase(
            RefreshSessionRepository refreshSessionRepository,
            RefreshTokenRepository refreshTokenRepository,
            RefreshTokenHasher refreshTokenHasher
    ) {
        if (refreshSessionRepository == null) {
            throw new IllegalArgumentException("refreshSessionRepository must not be null");
        }
        if (refreshTokenRepository == null) {
            throw new IllegalArgumentException("refreshTokenRepository must not be null");
        }
        if (refreshTokenHasher == null) {
            throw new IllegalArgumentException("refreshTokenHasher must not be null");
        }
        this.refreshSessionRepository = refreshSessionRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenHasher = refreshTokenHasher;
    }

    void execute(String plaintextRefreshToken, Instant now) {
        if (plaintextRefreshToken == null || plaintextRefreshToken.isBlank()) {
            throw new RefreshAuthenticationException("refresh token is invalid");
        }
        if (now == null) {
            throw new IllegalArgumentException("now must not be null");
        }
        String hash = refreshTokenHasher.hash(plaintextRefreshToken);
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new RefreshAuthenticationException("refresh token is invalid"));
        RefreshSession session = refreshSessionRepository.findById(token.getSessionId())
                .orElseThrow(() -> new RefreshAuthenticationException("refresh token is invalid"));
        if (session.isRevoked()) {
            return;
        }
        if (session.isExpired(now)) {
            return;
        }
        refreshSessionRepository.save(session.revoke(LOGOUT_REASON, now));
    }
}

