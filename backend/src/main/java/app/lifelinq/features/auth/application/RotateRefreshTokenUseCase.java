package app.lifelinq.features.auth.application;

import app.lifelinq.features.auth.domain.RefreshSession;
import app.lifelinq.features.auth.domain.RefreshSessionRepository;
import app.lifelinq.features.auth.domain.RefreshToken;
import app.lifelinq.features.auth.domain.RefreshTokenGenerator;
import app.lifelinq.features.auth.domain.RefreshTokenHasher;
import app.lifelinq.features.auth.domain.RefreshTokenRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.dao.OptimisticLockingFailureException;

final class RotateRefreshTokenUseCase {
    private static final String REPLAY_REASON = "REPLAY";

    private final RefreshSessionRepository refreshSessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final RefreshTokenHasher refreshTokenHasher;

    RotateRefreshTokenUseCase(
            RefreshSessionRepository refreshSessionRepository,
            RefreshTokenRepository refreshTokenRepository,
            RefreshTokenGenerator refreshTokenGenerator,
            RefreshTokenHasher refreshTokenHasher
    ) {
        if (refreshSessionRepository == null) {
            throw new IllegalArgumentException("refreshSessionRepository must not be null");
        }
        if (refreshTokenRepository == null) {
            throw new IllegalArgumentException("refreshTokenRepository must not be null");
        }
        if (refreshTokenGenerator == null) {
            throw new IllegalArgumentException("refreshTokenGenerator must not be null");
        }
        if (refreshTokenHasher == null) {
            throw new IllegalArgumentException("refreshTokenHasher must not be null");
        }
        this.refreshSessionRepository = refreshSessionRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.refreshTokenHasher = refreshTokenHasher;
    }

    RotateRefreshTokenResult execute(String plaintextRefreshToken, Instant now, Duration idleTtl) {
        if (plaintextRefreshToken == null || plaintextRefreshToken.isBlank()) {
            throw new RefreshAuthenticationException("refresh token is invalid");
        }
        if (now == null) {
            throw new IllegalArgumentException("now must not be null");
        }
        if (idleTtl == null || idleTtl.isZero() || idleTtl.isNegative()) {
            throw new IllegalArgumentException("idleTtl must be positive");
        }

        String tokenHash = refreshTokenHasher.hash(plaintextRefreshToken);
        RefreshToken currentToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new RefreshAuthenticationException("refresh token is invalid"));

        RefreshSession session = refreshSessionRepository.findById(currentToken.getSessionId())
                .orElseThrow(() -> new RefreshAuthenticationException("refresh token is invalid"));

        if (session.isRevoked() || session.isExpired(now)) {
            throw new RefreshAuthenticationException("refresh token is invalid");
        }
        if (currentToken.isExpired(now) || currentToken.isRevoked()) {
            throw new RefreshAuthenticationException("refresh token is invalid");
        }

        UUID replacementId = UUID.randomUUID();
        RefreshToken consumedToken;
        try {
            consumedToken = currentToken.replaceWith(replacementId, now);
            refreshTokenRepository.save(consumedToken);
        } catch (IllegalStateException | OptimisticLockingFailureException ex) {
            revokeSessionForReplay(session, now);
            throw new RefreshAuthenticationException("refresh token is invalid");
        }

        String nextPlaintext = refreshTokenGenerator.generate();
        String nextHash = refreshTokenHasher.hash(nextPlaintext);
        Instant nextIdleExpiry = min(now.plus(idleTtl), session.getAbsoluteExpiresAt());
        RefreshToken replacementToken = new RefreshToken(
                replacementId,
                session.getId(),
                nextHash,
                now,
                nextIdleExpiry,
                null,
                null,
                null,
                null
        );
        refreshTokenRepository.save(replacementToken);
        return new RotateRefreshTokenResult(session.getUserId(), nextPlaintext);
    }

    private void revokeSessionForReplay(RefreshSession session, Instant now) {
        try {
            RefreshSession latest = refreshSessionRepository.findById(session.getId()).orElse(session);
            if (!latest.isRevoked() && !latest.isExpired(now)) {
                refreshSessionRepository.save(latest.revoke(REPLAY_REASON, now));
            }
        } catch (RuntimeException ignored) {
            // Best-effort replay hardening; refresh remains rejected regardless.
        }
    }

    private Instant min(Instant left, Instant right) {
        return left.isBefore(right) ? left : right;
    }
}
