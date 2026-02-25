package app.lifelinq.features.group.contract;

import java.util.UUID;

public interface EnsureGroupMemberUseCase {
    void execute(UUID groupId, UUID actorUserId);
}
