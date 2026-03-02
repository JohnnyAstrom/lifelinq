package app.lifelinq.features.group.infrastructure;

import app.lifelinq.features.group.application.GroupApplicationService;
import app.lifelinq.features.user.contract.UserActiveGroupRead;
import app.lifelinq.features.user.contract.UserActiveGroupSelection;
import app.lifelinq.features.user.contract.UserProfileRead;
import app.lifelinq.features.user.contract.UserProvisioning;
import java.time.Clock;

public final class GroupInMemoryWiring {
    private GroupInMemoryWiring() {
    }

    public static GroupApplicationService createApplicationService(
            UserProvisioning userProvisioning,
            UserActiveGroupRead userActiveGroupRead,
            UserActiveGroupSelection userActiveGroupSelection,
            UserProfileRead userProfileRead
    ) {
        GroupPersistenceAdapter adapter = new GroupPersistenceAdapter();
        InMemoryInvitationRepository invitationRepository = new InMemoryInvitationRepository();
        InMemoryInvitationTokenGenerator tokenGenerator = new InMemoryInvitationTokenGenerator();
        return GroupApplicationService.create(
                adapter,
                adapter,
                invitationRepository,
                tokenGenerator,
                userProvisioning,
                userActiveGroupRead,
                userActiveGroupSelection,
                userProfileRead,
                Clock.systemUTC()
        );
    }
}
