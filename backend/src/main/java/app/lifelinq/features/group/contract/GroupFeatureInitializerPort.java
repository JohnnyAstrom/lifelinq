package app.lifelinq.features.group.contract;

import java.util.UUID;

public interface GroupFeatureInitializerPort {
    void initialize(UUID groupId);
}
