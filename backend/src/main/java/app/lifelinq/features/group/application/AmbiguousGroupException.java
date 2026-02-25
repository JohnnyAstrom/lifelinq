package app.lifelinq.features.group.application;

public final class AmbiguousGroupException extends RuntimeException {
    public AmbiguousGroupException(String message) {
        super(message);
    }
}
