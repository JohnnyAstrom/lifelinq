package app.lifelinq.features.household.application;

public final class AmbiguousHouseholdException extends RuntimeException {
    public AmbiguousHouseholdException(String message) {
        super(message);
    }
}
