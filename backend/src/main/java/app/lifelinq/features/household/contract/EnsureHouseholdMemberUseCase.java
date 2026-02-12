package app.lifelinq.features.household.contract;

import java.util.UUID;

public interface EnsureHouseholdMemberUseCase {
    void execute(UUID householdId, UUID actorUserId);
}
