package app.lifelinq.features.auth.application;

public final class VerifiedMagicLinkResult {
    private final String normalizedEmail;

    public VerifiedMagicLinkResult(String normalizedEmail) {
        this.normalizedEmail = normalizedEmail;
    }

    public String getNormalizedEmail() {
        return normalizedEmail;
    }
}

