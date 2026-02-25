package app.lifelinq.features.auth.contract;

import java.util.UUID;

public record UserMembershipView(UUID groupId, String role) {
}
