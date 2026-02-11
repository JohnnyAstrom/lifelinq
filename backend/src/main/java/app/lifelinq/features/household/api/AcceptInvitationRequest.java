package app.lifelinq.features.household.api;

import java.util.UUID;

public final class AcceptInvitationRequest {
    private String token;
    private UUID userId;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
