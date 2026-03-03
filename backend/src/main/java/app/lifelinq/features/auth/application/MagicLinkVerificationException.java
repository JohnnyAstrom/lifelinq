package app.lifelinq.features.auth.application;

public class MagicLinkVerificationException extends RuntimeException {
    public MagicLinkVerificationException(String message) {
        super(message);
    }
}

