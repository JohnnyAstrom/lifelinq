package app.lifelinq.features.household.api;

public final class RemoveMemberResponse {
    private final boolean removed;

    public RemoveMemberResponse(boolean removed) {
        this.removed = removed;
    }

    public boolean isRemoved() {
        return removed;
    }
}
