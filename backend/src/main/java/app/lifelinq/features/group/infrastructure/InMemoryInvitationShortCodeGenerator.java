package app.lifelinq.features.group.infrastructure;

import app.lifelinq.features.group.application.InvitationShortCodeGenerator;
import java.security.SecureRandom;

public final class InMemoryInvitationShortCodeGenerator implements InvitationShortCodeGenerator {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int LENGTH = 6;

    private final SecureRandom random = new SecureRandom();

    @Override
    public String generate() {
        char[] chars = new char[LENGTH];
        for (int i = 0; i < LENGTH; i++) {
            chars[i] = ALPHABET.charAt(random.nextInt(ALPHABET.length()));
        }
        return new String(chars);
    }
}
