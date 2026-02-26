package app.lifelinq.features.group.contract;

import java.util.UUID;

public interface UserDefaultGroupProvisioning {
    UUID ensureDefaultGroupProvisioned(UUID userId);
}
