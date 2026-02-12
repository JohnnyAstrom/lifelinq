package app.lifelinq.features.shopping.application;

public final class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
