package app.lifelinq.features.user.contract;

import java.util.UUID;

public interface UserProvisioning {
    void ensureUserExists(UUID userId);
}
