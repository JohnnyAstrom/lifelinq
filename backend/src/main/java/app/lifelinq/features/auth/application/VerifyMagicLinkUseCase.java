package app.lifelinq.features.auth.application;

import app.lifelinq.features.auth.domain.MagicLinkChallenge;
import app.lifelinq.features.auth.domain.MagicLinkChallengeRepository;
import java.util.Locale;
import org.springframework.dao.OptimisticLockingFailureException;

final class VerifyMagicLinkUseCase {
    private static final int EXPECTED_TOKEN_LENGTH = 48;
    private final MagicLinkChallengeRepository challengeRepository;

    VerifyMagicLinkUseCase(MagicLinkChallengeRepository challengeRepository) {
        if (challengeRepository == null) {
            throw new IllegalArgumentException("challengeRepository must not be null");
        }
        this.challengeRepository = challengeRepository;
    }

    VerifiedMagicLinkResult execute(VerifyMagicLinkCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        String normalizedToken = normalizeAndValidateToken(command.getToken());
        if (normalizedToken == null) {
            throw new MagicLinkVerificationException("magic link is invalid or expired");
        }
        if (command.getNow() == null) {
            throw new IllegalArgumentException("now must not be null");
        }

        MagicLinkChallenge challenge = challengeRepository.findByToken(normalizedToken)
                .orElseThrow(() -> new MagicLinkVerificationException("magic link is invalid or expired"));

        if (challenge.isConsumed()) {
            throw new MagicLinkVerificationException("magic link is invalid or expired");
        }
        if (challenge.isExpired(command.getNow())) {
            throw new MagicLinkVerificationException("magic link is invalid or expired");
        }

        try {
            challengeRepository.save(challenge.consume(command.getNow()));
        } catch (OptimisticLockingFailureException ex) {
            throw new MagicLinkVerificationException("magic link is invalid or expired");
        }
        return new VerifiedMagicLinkResult(challenge.getEmail().trim().toLowerCase(Locale.ROOT));
    }

    private String normalizeAndValidateToken(String token) {
        if (token == null) {
            return null;
        }
        String normalized = token.trim();
        if (normalized.isEmpty() || normalized.length() != EXPECTED_TOKEN_LENGTH) {
            return null;
        }
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            if (!Character.isLetterOrDigit(ch)) {
                return null;
            }
        }
        return normalized;
    }
}
