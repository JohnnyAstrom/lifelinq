package app.lifelinq.features.user.contract;

public final class DeleteAccountBlockedException extends RuntimeException {
    public DeleteAccountBlockedException(String message) {
        super(message);
    }
}
