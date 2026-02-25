package app.lifelinq.features.auth.application;

public final class ActiveGroupSelectionConflictException extends RuntimeException {
    public ActiveGroupSelectionConflictException(String message) {
        super(message);
    }
}
