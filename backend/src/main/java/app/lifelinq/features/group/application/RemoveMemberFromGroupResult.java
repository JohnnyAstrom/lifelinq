package app.lifelinq.features.group.application;

public final class RemoveMemberFromGroupResult {
    private final boolean removed;

    public RemoveMemberFromGroupResult(boolean removed) {
        this.removed = removed;
    }

    public boolean isRemoved() {
        return removed;
    }
}
