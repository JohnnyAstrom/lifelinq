package app.lifelinq.features.auth.application;

import app.lifelinq.features.auth.domain.MagicLinkChallenge;
import app.lifelinq.features.auth.domain.MagicLinkChallengeRepository;
import java.util.Locale;

final class VerifyMagicLinkUseCase {
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
        if (command.getToken() == null || command.getToken().isBlank()) {
            throw new MagicLinkVerificationException("magic link is invalid or expired");
        }
        if (command.getNow() == null) {
            throw new IllegalArgumentException("now must not be null");
        }

        MagicLinkChallenge challenge = challengeRepository.findByToken(command.getToken().trim())
                .orElseThrow(() -> new MagicLinkVerificationException("magic link is invalid or expired"));

        if (challenge.isConsumed()) {
            throw new MagicLinkVerificationException("magic link is invalid or expired");
        }
        if (challenge.isExpired(command.getNow())) {
            throw new MagicLinkVerificationException("magic link is invalid or expired");
        }

        challengeRepository.save(challenge.consume(command.getNow()));
        return new VerifiedMagicLinkResult(challenge.getEmail().trim().toLowerCase(Locale.ROOT));
    }
}

