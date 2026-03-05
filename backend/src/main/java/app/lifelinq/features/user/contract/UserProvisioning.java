package app.lifelinq.features.user.contract;

import java.util.UUID;

public interface UserProvisioning {
    UUID ensureUserExistsAndResolveUserId(UUID proposedUserId, String email);

    default void ensureUserExists(UUID userId) {
        ensureUserExistsAndResolveUserId(userId, null);
    }

    default void ensureUserExists(UUID userId, String email) {
        ensureUserExistsAndResolveUserId(userId, email);
    }
}
