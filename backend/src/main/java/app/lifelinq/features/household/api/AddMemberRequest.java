package app.lifelinq.features.household.api;

import java.util.UUID;

public final class AddMemberRequest {
    private UUID userId;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
