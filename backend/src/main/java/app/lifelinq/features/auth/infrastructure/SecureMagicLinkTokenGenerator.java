package app.lifelinq.features.auth.infrastructure;

import app.lifelinq.features.auth.domain.MagicLinkTokenGenerator;
import java.security.SecureRandom;

public final class SecureMagicLinkTokenGenerator implements MagicLinkTokenGenerator {
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int TOKEN_LENGTH = 48;

    private final SecureRandom random = new SecureRandom();

    @Override
    public String generate() {
        char[] chars = new char[TOKEN_LENGTH];
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            chars[i] = ALPHABET.charAt(random.nextInt(ALPHABET.length()));
        }
        return new String(chars);
    }
}

