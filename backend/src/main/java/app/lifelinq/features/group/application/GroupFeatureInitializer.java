package app.lifelinq.features.group.application;

import app.lifelinq.features.group.contract.GroupFeatureInitializerPort;
import java.util.List;
import java.util.UUID;

public final class GroupFeatureInitializer {
    private final List<GroupFeatureInitializerPort> featureInitializers;

    public GroupFeatureInitializer(List<GroupFeatureInitializerPort> featureInitializers) {
        if (featureInitializers == null) {
            throw new IllegalArgumentException("featureInitializers must not be null");
        }
        this.featureInitializers = List.copyOf(featureInitializers);
    }

    public void initializeFeatures(UUID groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        for (GroupFeatureInitializerPort initializer : featureInitializers) {
            initializer.initialize(groupId);
        }
    }
}
