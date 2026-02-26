package app.lifelinq.features.user.contract;

public class InvalidUserProfileException extends RuntimeException {
    public InvalidUserProfileException(String message) {
        super(message);
    }
}
