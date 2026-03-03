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

final class IssueRefreshSessionUseCase {
    private final RefreshSessionRepository refreshSessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final RefreshTokenHasher refreshTokenHasher;

    IssueRefreshSessionUseCase(
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

    IssueRefreshSessionResult execute(UUID userId, Instant now, Duration idleTtl, Duration absoluteTtl) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        if (now == null) {
            throw new IllegalArgumentException("now must not be null");
        }
        if (idleTtl == null || idleTtl.isZero() || idleTtl.isNegative()) {
            throw new IllegalArgumentException("idleTtl must be positive");
        }
        if (absoluteTtl == null || absoluteTtl.isZero() || absoluteTtl.isNegative()) {
            throw new IllegalArgumentException("absoluteTtl must be positive");
        }
        if (idleTtl.compareTo(absoluteTtl) > 0) {
            throw new IllegalArgumentException("idleTtl must not exceed absoluteTtl");
        }

        UUID sessionId = UUID.randomUUID();
        Instant absoluteExpiresAt = now.plus(absoluteTtl);
        RefreshSession session = new RefreshSession(
                sessionId,
                userId,
                now,
                absoluteExpiresAt,
                null,
                null,
                null
        );
        refreshSessionRepository.save(session);

        String plaintextRefresh = refreshTokenGenerator.generate();
        String refreshHash = refreshTokenHasher.hash(plaintextRefresh);
        Instant idleExpiresAt = min(now.plus(idleTtl), absoluteExpiresAt);
        RefreshToken token = new RefreshToken(
                UUID.randomUUID(),
                sessionId,
                refreshHash,
                now,
                idleExpiresAt,
                null,
                null,
                null,
                null
        );
        refreshTokenRepository.save(token);
        return new IssueRefreshSessionResult(sessionId, plaintextRefresh);
    }

    private Instant min(Instant left, Instant right) {
        return left.isBefore(right) ? left : right;
    }
}

