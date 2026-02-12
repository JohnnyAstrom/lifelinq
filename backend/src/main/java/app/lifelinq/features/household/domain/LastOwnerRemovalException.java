package app.lifelinq.features.household.domain;

public class LastOwnerRemovalException extends RuntimeException {
    public LastOwnerRemovalException(String message) {
        super(message);
    }
}
