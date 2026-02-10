package app.lifelinq.features.household.application;

public final class RemoveMemberFromHouseholdResult {
    private final boolean removed;

    public RemoveMemberFromHouseholdResult(boolean removed) {
        this.removed = removed;
    }

    public boolean isRemoved() {
        return removed;
    }
}
