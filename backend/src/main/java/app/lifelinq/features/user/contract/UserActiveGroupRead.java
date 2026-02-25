package app.lifelinq.features.user.contract;

import java.util.UUID;

public interface UserActiveGroupRead {
    UUID getActiveGroupId(UUID userId);
}
