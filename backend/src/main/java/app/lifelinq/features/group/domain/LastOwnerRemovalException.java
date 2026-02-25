package app.lifelinq.features.group.domain;

public class LastOwnerRemovalException extends RuntimeException {
    public LastOwnerRemovalException(String message) {
        super(message);
    }
}
