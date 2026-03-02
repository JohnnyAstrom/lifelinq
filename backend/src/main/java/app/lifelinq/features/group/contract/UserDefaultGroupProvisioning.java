package app.lifelinq.features.group.contract;

import java.util.UUID;

public interface UserDefaultGroupProvisioning {
    default UUID ensureDefaultGroupProvisioned(UUID userId) {
        return ensureDefaultGroupProvisioned(userId, null);
    }

    UUID ensureDefaultGroupProvisioned(UUID userId, String initialPlaceName);
}
