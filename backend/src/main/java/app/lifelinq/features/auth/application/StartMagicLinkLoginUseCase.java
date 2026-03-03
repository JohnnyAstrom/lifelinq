package app.lifelinq.features.auth.application;

import app.lifelinq.features.auth.domain.AuthMailSender;
import app.lifelinq.features.auth.domain.MagicLinkChallenge;
import app.lifelinq.features.auth.domain.MagicLinkChallengeRepository;
import app.lifelinq.features.auth.domain.MagicLinkTokenGenerator;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

final class StartMagicLinkLoginUseCase {
    private static final int MAX_TOKEN_ATTEMPTS = 10;

    private final MagicLinkChallengeRepository challengeRepository;
    private final MagicLinkTokenGenerator tokenGenerator;
    private final AuthMailSender mailSender;

    StartMagicLinkLoginUseCase(
            MagicLinkChallengeRepository challengeRepository,
            MagicLinkTokenGenerator tokenGenerator,
            AuthMailSender mailSender
    ) {
        if (challengeRepository == null) {
            throw new IllegalArgumentException("challengeRepository must not be null");
        }
        if (tokenGenerator == null) {
            throw new IllegalArgumentException("tokenGenerator must not be null");
        }
        if (mailSender == null) {
            throw new IllegalArgumentException("mailSender must not be null");
        }
        this.challengeRepository = challengeRepository;
        this.tokenGenerator = tokenGenerator;
        this.mailSender = mailSender;
    }

    void execute(StartMagicLinkLoginCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.getEmail() == null || command.getEmail().isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
        if (command.getNow() == null) {
            throw new IllegalArgumentException("now must not be null");
        }
        if (command.getTtl() == null || command.getTtl().isZero() || command.getTtl().isNegative()) {
            throw new IllegalArgumentException("ttl must be positive");
        }
        if (command.getVerifyBaseUrl() == null || command.getVerifyBaseUrl().isBlank()) {
            throw new IllegalArgumentException("verifyBaseUrl must not be blank");
        }

        String normalizedEmail = normalizeEmail(command.getEmail());
        String token = generateUniqueToken();

        MagicLinkChallenge challenge = new MagicLinkChallenge(
                UUID.randomUUID(),
                token,
                normalizedEmail,
                command.getNow().plus(command.getTtl()),
                null
        );
        challengeRepository.save(challenge);

        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        String separator = command.getVerifyBaseUrl().contains("?") ? "&" : "?";
        String verifyUrl = command.getVerifyBaseUrl() + separator + "token=" + encodedToken;
        mailSender.sendMagicLink(normalizedEmail, verifyUrl);
    }

    private String generateUniqueToken() {
        for (int attempt = 0; attempt < MAX_TOKEN_ATTEMPTS; attempt++) {
            String token = tokenGenerator.generate();
            if (token == null || token.isBlank()) {
                throw new IllegalArgumentException("generated token must not be blank");
            }
            if (!challengeRepository.existsByToken(token)) {
                return token;
            }
        }
        throw new IllegalStateException("could not generate unique magic link token");
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

