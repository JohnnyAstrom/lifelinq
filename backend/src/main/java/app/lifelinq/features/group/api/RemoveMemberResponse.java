package app.lifelinq.features.group.api;

public final class RemoveMemberResponse {
    private final boolean removed;

    public RemoveMemberResponse(boolean removed) {
        this.removed = removed;
    }

    public boolean isRemoved() {
        return removed;
    }
}
