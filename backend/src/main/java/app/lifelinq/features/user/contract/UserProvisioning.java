package app.lifelinq.features.user.contract;

import java.util.UUID;

public interface UserProvisioning {
    void ensureUserExists(UUID userId);

    default void ensureUserExists(UUID userId, String normalizedEmail) {
        ensureUserExists(userId);
    }
}
