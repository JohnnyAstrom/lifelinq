package app.lifelinq.features.group.application;

public final class RevokeInvitationResult {
    private final boolean revoked;

    public RevokeInvitationResult(boolean revoked) {
        this.revoked = revoked;
    }

    public boolean isRevoked() {
        return revoked;
    }
}
