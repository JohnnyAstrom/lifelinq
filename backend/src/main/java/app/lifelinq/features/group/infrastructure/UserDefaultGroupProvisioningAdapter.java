package app.lifelinq.features.group.infrastructure;

import app.lifelinq.features.group.application.GroupApplicationService;
import app.lifelinq.features.group.contract.UserDefaultGroupProvisioning;
import java.util.UUID;

public final class UserDefaultGroupProvisioningAdapter implements UserDefaultGroupProvisioning {
    private final GroupApplicationService groupApplicationService;

    public UserDefaultGroupProvisioningAdapter(GroupApplicationService groupApplicationService) {
        if (groupApplicationService == null) {
            throw new IllegalArgumentException("groupApplicationService must not be null");
        }
        this.groupApplicationService = groupApplicationService;
    }

    @Override
    public UUID ensureDefaultGroupProvisioned(UUID userId) {
        return groupApplicationService.ensureDefaultGroupProvisioned(userId);
    }
}
