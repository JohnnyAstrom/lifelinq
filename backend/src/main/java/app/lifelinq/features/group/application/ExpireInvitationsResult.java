package app.lifelinq.features.group.application;

public final class ExpireInvitationsResult {
    private final int expiredCount;

    public ExpireInvitationsResult(int expiredCount) {
        this.expiredCount = expiredCount;
    }

    public int getExpiredCount() {
        return expiredCount;
    }
}
