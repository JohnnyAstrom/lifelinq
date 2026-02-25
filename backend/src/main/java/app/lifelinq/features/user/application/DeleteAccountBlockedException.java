package app.lifelinq.features.user.application;

public final class DeleteAccountBlockedException extends RuntimeException {
    public DeleteAccountBlockedException(String message) {
        super(message);
    }
}
